package nl.bluetainer.jbeancount.io;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import nl.bluetainer.jbeancount.annotation.Beta;
import nl.bluetainer.jbeancount.language.Account;
import nl.bluetainer.jbeancount.language.AdditionExpression;
import nl.bluetainer.jbeancount.language.Amount;
import nl.bluetainer.jbeancount.language.ArithmeticExpression;
import nl.bluetainer.jbeancount.language.BalanceDirective;
import nl.bluetainer.jbeancount.language.BinaryCompoundExpression;
import nl.bluetainer.jbeancount.language.BooleanValue;
import nl.bluetainer.jbeancount.language.CloseDirective;
import nl.bluetainer.jbeancount.language.Comment;
import nl.bluetainer.jbeancount.language.Commodity;
import nl.bluetainer.jbeancount.language.CommodityDirective;
import nl.bluetainer.jbeancount.language.CompoundAmount;
import nl.bluetainer.jbeancount.language.CompoundExpression;
import nl.bluetainer.jbeancount.language.ConstantExpression;
import nl.bluetainer.jbeancount.language.CostSpec;
import nl.bluetainer.jbeancount.language.CustomDirective;
import nl.bluetainer.jbeancount.language.DateValue;
import nl.bluetainer.jbeancount.language.DirectiveNode;
import nl.bluetainer.jbeancount.language.DivisionExpression;
import nl.bluetainer.jbeancount.language.Eol;
import nl.bluetainer.jbeancount.language.EventDirective;
import nl.bluetainer.jbeancount.language.Flag;
import nl.bluetainer.jbeancount.language.IncludePragma;
import nl.bluetainer.jbeancount.language.Journal;
import nl.bluetainer.jbeancount.language.JournalDeclaration;
import nl.bluetainer.jbeancount.language.Link;
import nl.bluetainer.jbeancount.language.LinkValue;
import nl.bluetainer.jbeancount.language.Metadata;
import nl.bluetainer.jbeancount.language.MetadataItem;
import nl.bluetainer.jbeancount.language.MetadataKey;
import nl.bluetainer.jbeancount.language.MetadataLine;
import nl.bluetainer.jbeancount.language.MetadataValue;
import nl.bluetainer.jbeancount.language.MultiplicationExpression;
import nl.bluetainer.jbeancount.language.NegationExpression;
import nl.bluetainer.jbeancount.language.NilValue;
import nl.bluetainer.jbeancount.language.NoteDirective;
import nl.bluetainer.jbeancount.language.OpenDirective;
import nl.bluetainer.jbeancount.language.OptionPragma;
import nl.bluetainer.jbeancount.language.PadDirective;
import nl.bluetainer.jbeancount.language.ParenthesisedExpression;
import nl.bluetainer.jbeancount.language.PluginPragma;
import nl.bluetainer.jbeancount.language.PlusExpression;
import nl.bluetainer.jbeancount.language.PopTagPragma;
import nl.bluetainer.jbeancount.language.Posting;
import nl.bluetainer.jbeancount.language.PriceAnnotation;
import nl.bluetainer.jbeancount.language.PriceDirective;
import nl.bluetainer.jbeancount.language.PushTagPragma;
import nl.bluetainer.jbeancount.language.QueryDirective;
import nl.bluetainer.jbeancount.language.StringValue;
import nl.bluetainer.jbeancount.language.SubtractionExpression;
import nl.bluetainer.jbeancount.language.Tag;
import nl.bluetainer.jbeancount.language.TagOrLink;
import nl.bluetainer.jbeancount.language.TagValue;
import nl.bluetainer.jbeancount.language.TransactionDirective;
import nl.bluetainer.jbeancount.language.UnaryCompoundExpression;
import nl.bluetainer.jbeancount.util.Assert;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class SimpleBeancountPrinter implements BeancountPrinter {

  private static final Pattern VALID_INDENTATION_SEQUENCE_PATTERN = Pattern.compile("^[ \t]+$");

  public static Builder newPrinter() {
    return new Builder();
  }

  @Beta
  public static SimpleBeancountPrinter newDefaultPrinter() {
    return newPrinter()
        .indentationSequence("  ") // Two spaces
        .currencyColumn(61) // TODO This is temporary, infer currency column from file context
        .compactMode(false)
        .build();
  }

  @Beta
  public static SimpleBeancountPrinter newFavaPrinter() {
    return newPrinter()
        .indentationSequence("  ") // Two spaces
        .currencyColumn(61) // Fava default
        .compactMode(false)
        .build();
  }

  private final int currencyColumn;
  private final String indentationSequence;
  private final boolean compactMode;

  // Mutable values
  private int nestingLevel = 0;
  private String indent;

  // Two levels of indentation is often the maximum (metadata on postings is the 2nd level)
  private String[] indents = new String[2];

  private SimpleBeancountPrinter(
      int currencyColumn, String indentationSequence, boolean compactMode) {
    if (currencyColumn < 0) {
      throw new IllegalArgumentException("The currency column must be positive");
    }
    if (indentationSequence.isEmpty()) {
      throw new IllegalArgumentException("The indentation sequence must be nonempty whitespace");
    }
    if (!VALID_INDENTATION_SEQUENCE_PATTERN.asMatchPredicate().test(indentationSequence)) {
      throw new IllegalArgumentException(
          "The indentation sequence must be composed of spaces and/or tabs");
    }
    this.currencyColumn = currencyColumn;
    this.indentationSequence = indentationSequence;
    this.compactMode = compactMode;
  }

  public static final class Builder {
    private int currencyColumn;
    private String indentationSequence;
    private boolean compactMode;

    private Builder() {}

    public SimpleBeancountPrinter build() {
      return new SimpleBeancountPrinter(currencyColumn, indentationSequence, compactMode);
    }

    public int currencyColumn() {
      return currencyColumn;
    }

    public Builder currencyColumn(int currencyColumn) {
      this.currencyColumn = currencyColumn;
      return this;
    }

    public String indentationSequence() {
      return indentationSequence;
    }

    public Builder indentationSequence(String indentationSequence) {
      this.indentationSequence = indentationSequence;
      return this;
    }

    public boolean compactMode() {
      return compactMode;
    }

    public Builder compactMode(boolean compactMode) {
      this.compactMode = compactMode;
      return this;
    }
  }

  private void indent() {
    nestingLevel++;
    maybeResizeIndents();
    updateIndent();
  }

  private void dedent() {
    nestingLevel--;
    updateIndent();
  }

  private void maybeResizeIndents() {
    if (indents.length <= nestingLevel) {
      indents = Arrays.copyOf(indents, indents.length + 1);
    }
  }

  private void updateIndent() {
    this.indent =
        Objects.requireNonNullElseGet(
            indents[nestingLevel],
            () -> indents[nestingLevel] = indentationSequence.repeat(nestingLevel));
  }

  private BeancountIOException typeNotHandled(Object type) {
    if (type == null) {
      return new BeancountIOException("This printer cannot handle null values");
    }
    return new BeancountIOException("This printer cannot handle " + type.getClass().getName());
  }

  private void dent(PrintWriter pw) {
    pw.print(indent);
  }

  private void nl(PrintWriter pw) {
    pw.print('\n');
  }

  private void space(PrintWriter pw) {
    pw.print(' ');
  }

  private void quote(PrintWriter pw) {
    pw.print('"');
  }

  @Override
  public @NotNull String print(@NotNull Journal journal) throws BeancountIOException {
    Objects.requireNonNull(journal, "journal");

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);

    for (JournalDeclaration<?, ?> declaration : journal.declarations()) {
      if (declaration instanceof BalanceDirective bd) {
        print(pw, bd);
      } else if (declaration instanceof PadDirective pd) {
        print(pw, pd);
      } else if (declaration instanceof TransactionDirective td) {
        print(pw, td);
      } else if (declaration instanceof OpenDirective od) {
        print(pw, od);
      } else if (declaration instanceof CloseDirective cd) {
        print(pw, cd);
      } else if (declaration instanceof EventDirective ed) {
        print(pw, ed);
      } else if (declaration instanceof PriceDirective pd) {
        print(pw, pd);
      } else if (declaration instanceof NoteDirective nd) {
        print(pw, nd);
      } else if (declaration instanceof CommodityDirective cd) {
        print(pw, cd);
      } else if (declaration instanceof QueryDirective qd) {
        print(pw, qd);
      } else if (declaration instanceof CustomDirective cd) {
        print(pw, cd);
      } else if (declaration instanceof IncludePragma ip) {
        print(pw, ip);
      } else if (declaration instanceof OptionPragma op) {
        print(pw, op);
      } else if (declaration instanceof PluginPragma pp) {
        print(pw, pp);
      } else if (declaration instanceof PushTagPragma ptp) {
        print(pw, ptp);
      } else if (declaration instanceof PopTagPragma ptp) {
        print(pw, ptp);
      } else if (declaration instanceof Comment comment) {
        print(pw, comment);
      } else if (declaration instanceof Eol) {
        nl(pw);
      } else {
        throw typeNotHandled(declaration);
      }
    }

    return sw.toString();
  }

  private void print(PrintWriter pw, IncludePragma ip) {
    pw.write("include \"");
    pw.write(ip.filename());
    pw.write("\"\n");
  }

  private void print(PrintWriter pw, OptionPragma op) {
    pw.write("option \"");
    pw.write(op.name());
    pw.write("\" \"");
    pw.write(op.value());
    pw.write("\"\n");
  }

  private void print(PrintWriter pw, PluginPragma pp) {
    pw.write("plugin \"");
    pw.write(pp.name());
    if (pp.config() != null) {
      pw.write("\" \"");
      pw.write(pp.config());
    }
    pw.write("\"\n");
  }

  private void print(PrintWriter pw, PopTagPragma ptp) {
    throw new UnsupportedOperationException(
        "The poptag pragma is not yet supported by the printer");
  }

  private void print(PrintWriter pw, PushTagPragma ptp) {
    throw new UnsupportedOperationException(
        "The pushtag pragma is not yet supported by the printer");
  }

  private void print(PrintWriter pw, CustomDirective cd) {
    print(pw, cd.date());
    space(pw);
    pw.print("custom \"");
    pw.print(cd.name());
    pw.print('"');
    loop(cd.values(), () -> space(pw), v -> print(pw, v), () -> space(pw));
    // Custom handling because tags and links can be values, do not use printDirective
    final Comment comment = cd.comment();
    if (comment != null) {
      print(pw, comment);
    }
    nl(pw);
    print(pw, cd.metadata(), this::nl);
  }

  private void print(PrintWriter pw, QueryDirective qd) {
    print(pw, qd.date());
    pw.print(" query \"");
    pw.print(qd.name());
    pw.print("\" \"");
    pw.print(qd.sql());
    pw.print('"');
    printDirective(pw, qd);
  }

  private void print(PrintWriter pw, CommodityDirective cd) {
    print(pw, cd.date());
    pw.write(" commodity ");
    pw.write(cd.commodity().commodity());
    printDirective(pw, cd);
  }

  private void print(PrintWriter pw, NoteDirective nd) {
    print(pw, nd.date());
    pw.write(" note ");
    pw.write(nd.account().account());
    quote(pw);
    pw.write(nd.note());
    quote(pw);
    printDirective(pw, nd);
  }

  private String date(LocalDate date) {
    return DateTimeFormatter.ISO_DATE.format(date);
  }

  private String account(Account account) {
    return account.account();
  }

  private String commodity(Commodity commodity) {
    return commodity.commodity();
  }

  private void printDirective(PrintWriter pw, DirectiveNode<?, ?> node) {
    // Please note that the custom directive has different behaviour!
    print(pw, this::space, node.tagsAndLinks());
    final Comment comment = node.comment();
    if (comment != null) {
      space(pw);
      print(pw, comment);
    }
    nl(pw);
    print(pw, node.metadata(), this::nl);
  }

  private void print(PrintWriter pw, BalanceDirective bd) {
    final String account = account(bd.account());
    final String amount = arithmeticExpression(bd.amount().expression());
    final int col =
        currencyColumn
            - 10
            - 9 /* " balance " */
            - account.length()
            - 1 /* " " */
            - 2 /* start at 1, and space before commodity */;
    final String format = "%s balance %s %" + Math.max(col, 1) + "s %s";
    pw.format(format, date(bd.date()), account, amount, commodity(bd.amount().commodity()));
    printDirective(pw, bd);
  }

  private void print(PrintWriter pw, PadDirective pd) {
    final String sourceAccount = account(pd.sourceAccount());
    final String targetAccount = account(pd.targetAccount());
    pw.format("%s pad %s %s", date(pd.date()), sourceAccount, targetAccount);
    printDirective(pw, pd);
  }

  private void print(PrintWriter pw, EventDirective ed) {
    pw.format("%s event \"%s\" \"%s\"", date(ed.date()), ed.type(), ed.description());
    printDirective(pw, ed);
  }

  private void print(PrintWriter pw, LocalDate ld) {
    pw.print(date(ld));
  }

  private void print(PrintWriter pw, OpenDirective od) {
    print(pw, od.date());
    pw.print(" open ");
    print(pw, od.account());
    loop(od.commodities(), () -> space(pw), c -> print(pw, c), () -> pw.print(','));
    if (od.bookingMethod() != null) {
      space(pw);
      pw.write(od.bookingMethod());
    }
    printDirective(pw, od);
  }

  private void print(PrintWriter pw, CloseDirective cd) {
    print(pw, cd.date());
    pw.print(" close ");
    print(pw, cd.account());
    printDirective(pw, cd);
  }

  private void print(PrintWriter pw, PriceDirective pd) {
    final String commodity = commodity(pd.commodity());
    final String price = arithmeticExpression(pd.price().expression());
    final String otherCommodity = commodity(pd.price().commodity());
    final int col =
        currencyColumn
            - 10
            - 7 /* " price " */
            - commodity.length()
            - 1 /* " " */
            - 2 /* start at 1, and space before commodity */;
    final String format = "%s price %s %" + Math.max(col, 1) + "s %s";
    pw.format(format, date(pd.date()), commodity, price, otherCommodity);
    printDirective(pw, pd);
  }

  private void print(PrintWriter pw, TransactionDirective td) {
    pw.print(date(td.date()));
    space(pw);
    print(pw, td.flag());
    if (td.payee() != null) {
      space(pw);
      quote(pw);
      pw.print(td.payee());
      quote(pw);
      if (td.narration() == null) {
        pw.print(" \"\"");
      }
    }
    if (td.narration() != null) {
      space(pw);
      quote(pw);
      pw.print(td.narration());
      quote(pw);
    }
    printDirective(pw, td);
    final List<Posting> postings = td.postings();
    if (postings.isEmpty()) {
      return;
    }
    indent();
    dent(pw);
    loop(
        postings,
        p -> print(pw, p),
        () -> {
          nl(pw);
          dent(pw);
        });
    dedent();
    nl(pw);
    if (!compactMode) {
      nl(pw);
    }
  }

  private <T> void loop(
      Collection<T> collection, Consumer<T> onItemInLoop, Runnable afterEveryLoopBeforeLast) {
    loop(collection, null, onItemInLoop, afterEveryLoopBeforeLast);
  }

  private <T> void loop(
      Collection<T> collection,
      Runnable runBeforeIfNotEmpty,
      Consumer<T> onItemInLoop,
      Runnable afterEveryLoopBeforeLast) {
    if (collection == null || collection.isEmpty()) {
      return;
    }
    if (runBeforeIfNotEmpty != null) {
      runBeforeIfNotEmpty.run();
    }
    int size = collection.size();
    int i = 0;
    for (T t : collection) {
      onItemInLoop.accept(t);
      if (++i < size) {
        afterEveryLoopBeforeLast.run();
      }
    }
  }

  private void print(PrintWriter pw, Posting p) {
    //    if (p == null) {
    //      return; // TODO, this is a comment
    //    }
    final String account = p.account() == null ? "" : account(p.account());
    final ArithmeticExpression ae = p.amountExpression();
    final String num = ae == null ? "" : arithmeticExpression(ae);
    final Commodity c = p.commodity();
    final String commodity = c == null ? "" : commodity(c);
    int comp = 0;
    if (p.flag() != null) {
      print(pw, p.flag());
      comp = p.flag().flag().length();
    }
    if (num.isEmpty() && commodity.isEmpty()) {
      pw.print(account);
    } else {
      final int col =
          currencyColumn - account.length() - indentationSequence.length() - 1 /* " " */ - 2 - comp;
      final String format = "%s %" + Math.max(col, 1) + "s %s";
      pw.format(format, account, num, commodity);
    }
    final CostSpec cs = p.costSpec();
    if (cs != null) {
      space(pw);
      print(pw, cs);
    }
    final PriceAnnotation pa = p.priceAnnotation();
    if (pa != null) {
      space(pw);
      print(pw, pa);
    }
    final Comment comment = p.comment();
    if (comment != null) {
      if (p.account() != null) {
        space(pw);
      }
      print(pw, comment);
    }
    print(pw, this::nl, p.metadata());
  }

  private void print(PrintWriter pw, PriceAnnotation pa) {
    if (pa.totalCost()) {
      pw.print("@@");
    } else {
      pw.print('@');
    }
    if (pa.priceExpression() != null) {
      space(pw);
      print(pw, pa.priceExpression());
    }
    if (pa.commodity() != null) {
      space(pw);
      print(pw, pa.commodity());
    }
  }

  private void print(PrintWriter pw, CostSpec cs) {
    if (cs.doubleBraces()) {
      pw.print("{{");
    } else {
      pw.print('{');
    }
    loop(
        cs.components(),
        ccv -> {
          if (ccv instanceof CompoundAmount ca) {
            boolean space = false;
            final CompoundExpression ce = ca.compoundExpression();
            if (ce != null) {
              space = true;
              if (ce instanceof UnaryCompoundExpression uce) {
                print(pw, uce.expression());
              } else if (ce instanceof BinaryCompoundExpression uce) {
                final ArithmeticExpression le = uce.leftExpression();
                if (le != null) {
                  print(pw, le);
                  space(pw);
                }
                pw.print('#');
                final ArithmeticExpression re = uce.rightExpression();
                if (re != null) {
                  space(pw);
                  print(pw, re);
                }
              } else {
                Assert.shouldNeverHappen();
              }
            }
            if (ca.commodity() != null) {
              if (space) {
                space(pw);
              }
              print(pw, ca.commodity());
            }
          } else if (ccv instanceof DateValue dv) {
            print(pw, dv);
          } else if (ccv instanceof StringValue sv) {
            print(pw, sv);
          } else {
            throw typeNotHandled(ccv);
          }
        },
        () -> pw.print(", "));
    if (cs.doubleBraces()) {
      pw.print("}}");
    } else {
      pw.print('}');
    }
  }

  private void print(PrintWriter pw, Flag flag) {
    pw.print(flag.flag());
  }

  private void print(PrintWriter pw, Tag tag) {
    pw.print('#');
    pw.print(tag.tag());
  }

  private void print(PrintWriter pw, Link link) {
    pw.print('^');
    pw.print(link.link());
  }

  private void print(
      PrintWriter pw, Consumer<PrintWriter> beforeIfPresent, Collection<TagOrLink> tagsAndLinks) {
    if (!tagsAndLinks.isEmpty()) {
      beforeIfPresent.accept(pw);
    }
    print(pw, tagsAndLinks);
  }

  private void print(PrintWriter pw, Collection<TagOrLink> tagsAndLinks) {
    if (tagsAndLinks.isEmpty()) {
      return;
    }
    loop(
        tagsAndLinks,
        tagOrLink -> {
          if (tagOrLink instanceof Tag tag) {
            print(pw, tag);
          } else if (tagOrLink instanceof Link link) {
            print(pw, link);
          } else {
            throw typeNotHandled(tagOrLink);
          }
        },
        () -> space(pw));
  }

  private void print(PrintWriter pw, Metadata m, Consumer<PrintWriter> afterIfPresent) {
    print(pw, m, null, afterIfPresent);
  }

  private void print(PrintWriter pw, Consumer<PrintWriter> beforeIfPresent, Metadata m) {
    print(pw, m, beforeIfPresent, null);
  }

  private void print(
      PrintWriter pw,
      Metadata m,
      @Nullable Consumer<PrintWriter> beforeIfPresent,
      @Nullable Consumer<PrintWriter> afterIfPresent) {
    final List<MetadataLine> metadata = m.metadata();
    if (metadata.isEmpty()) {
      return;
    }
    if (beforeIfPresent != null) {
      beforeIfPresent.accept(pw);
    }
    indent();
    dent(pw);
    loop(
        metadata,
        l -> {
          if (l instanceof MetadataItem mi) {
            print(pw, mi.key());
            final MetadataValue mv = mi.value();
            if (mv != null && !mv.empty()) {
              space(pw); // Space between colon and value
              print(pw, mv);
            }
          } else if (l instanceof TagValue tv) {
            print(pw, tv);
          } else if (l instanceof LinkValue lv) {
            print(pw, lv);
          } else if (l instanceof Comment c) {
            print(pw, c);
          } else {
            throw typeNotHandled(l);
          }
        },
        () -> {
          nl(pw);
          dent(pw);
        });
    dedent();
    if (afterIfPresent != null) {
      afterIfPresent.accept(pw);
    }
  }

  private void print(PrintWriter pw, Comment comment) {
    pw.write(';');
    pw.write(comment.comment());
  }

  private String arithmeticExpression(ArithmeticExpression ae) {
    if (ae instanceof ConstantExpression ce) {
      return ce.value().toString();
    } else if (ae instanceof NegationExpression ne) {
      return "-" + arithmeticExpression(ne.expression());
    } else if (ae instanceof ParenthesisedExpression pe) {
      return "(" + arithmeticExpression(pe.expression()) + ")";
    } else if (ae instanceof PlusExpression pe) {
      return "+" + arithmeticExpression(pe.expression());
    } else if (ae instanceof AdditionExpression additionExpression) {
      return arithmeticExpression(additionExpression.leftExpression())
          + " + "
          + arithmeticExpression(additionExpression.rightExpression());
    } else if (ae instanceof SubtractionExpression se) {
      return arithmeticExpression(se.leftExpression())
          + " - "
          + arithmeticExpression(se.rightExpression());
    } else if (ae instanceof MultiplicationExpression me) {
      return arithmeticExpression(me.leftExpression())
          + " * "
          + arithmeticExpression(me.rightExpression());
    } else if (ae instanceof DivisionExpression de) {
      return arithmeticExpression(de.leftExpression())
          + " / "
          + arithmeticExpression(de.rightExpression());
    } else {
      throw typeNotHandled(ae);
    }
  }

  private void print(PrintWriter pw, MetadataKey mk) {
    pw.print(mk.key());
    pw.print(':');
  }

  private void print(PrintWriter pw, BooleanValue bv) {
    pw.print(String.valueOf(bv.value()).toUpperCase(Locale.ROOT));
  }

  private void print(PrintWriter pw, DateValue dv) {
    pw.print(date(dv.date()));
  }

  private void print(PrintWriter pw, StringValue sv) {
    quote(pw);
    pw.write(sv.value());
    quote(pw);
  }

  private void print(PrintWriter pw, LinkValue lv) {
    pw.write('^');
    pw.write(lv.link());
  }

  private void print(PrintWriter pw, TagValue tv) {
    pw.write('#');
    pw.write(tv.tag());
  }

  private void print(PrintWriter pw, Commodity c) {
    pw.write(commodity(c));
  }

  private void print(PrintWriter pw, Account a) {
    pw.write(account(a));
  }

  private void print(PrintWriter pw, ArithmeticExpression ae) {
    if (ae instanceof ConstantExpression ce) {
      pw.print(ce.value().toString());
    } else if (ae instanceof NegationExpression ne) {
      pw.print('-');
      print(pw, ne.expression());
    } else if (ae instanceof ParenthesisedExpression pe) {
      pw.write('(');
      print(pw, pe.expression());
      pw.write(')');
    } else if (ae instanceof PlusExpression pe) {
      pw.print('+');
      print(pw, pe.expression());
    } else if (ae instanceof AdditionExpression additionExpression) {
      print(pw, additionExpression.leftExpression());
      pw.print(" + ");
      print(pw, additionExpression.rightExpression());
    } else if (ae instanceof SubtractionExpression se) {
      print(pw, se.leftExpression());
      pw.print(" - ");
      print(pw, se.rightExpression());
    } else if (ae instanceof MultiplicationExpression me) {
      print(pw, me.leftExpression());
      pw.print(" * ");
      print(pw, me.rightExpression());
    } else if (ae instanceof DivisionExpression de) {
      print(pw, de.leftExpression());
      pw.print(" / ");
      print(pw, de.rightExpression());
    } else {
      throw typeNotHandled(ae);
    }
  }

  private void print(PrintWriter pw, MetadataValue mv) {
    if (mv instanceof Amount a) {
      pw.print(arithmeticExpression(a.expression()) + " " + commodity(a.commodity()));
    } else if (mv instanceof ArithmeticExpression ae) {
      print(pw, ae);
    } else if (mv instanceof BooleanValue bv) {
      print(pw, bv);
    } else if (mv instanceof Commodity c) {
      print(pw, c);
    } else if (mv instanceof DateValue dv) {
      print(pw, dv);
    } else if (mv instanceof LinkValue lv) {
      print(pw, lv);
    } else if (mv instanceof NilValue) {
      // Do nothing.
    } else if (mv instanceof StringValue sv) {
      print(pw, sv);
    } else if (mv instanceof TagValue tv) {
      print(pw, tv);
    } else if (mv instanceof Account a) {
      print(pw, a);
    } else {
      throw typeNotHandled(mv);
    }
  }
}
