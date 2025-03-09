package nl.bluetainer.jbeancount.language.tools;

import graphql.util.TraversalControl;
import graphql.util.TraverserContext;
import nl.bluetainer.jbeancount.language.BalanceDirective;
import nl.bluetainer.jbeancount.language.CloseDirective;
import nl.bluetainer.jbeancount.language.Comment;
import nl.bluetainer.jbeancount.language.CommodityDirective;
import nl.bluetainer.jbeancount.language.CustomDirective;
import nl.bluetainer.jbeancount.language.DocumentDirective;
import nl.bluetainer.jbeancount.language.EventDirective;
import nl.bluetainer.jbeancount.language.IncludePragma;
import nl.bluetainer.jbeancount.language.Journal;
import nl.bluetainer.jbeancount.language.Node;
import nl.bluetainer.jbeancount.language.NoteDirective;
import nl.bluetainer.jbeancount.language.OpenDirective;
import nl.bluetainer.jbeancount.language.OptionPragma;
import nl.bluetainer.jbeancount.language.PadDirective;
import nl.bluetainer.jbeancount.language.PluginPragma;
import nl.bluetainer.jbeancount.language.Posting;
import nl.bluetainer.jbeancount.language.PriceDirective;
import nl.bluetainer.jbeancount.language.QueryDirective;
import nl.bluetainer.jbeancount.language.TransactionDirective;

public class NodeVisitorStub implements NodeVisitor {
  @Override
  public TraversalControl visitIncludePragma(IncludePragma ip, TraverserContext<Node<?, ?>> data) {
    return TraversalControl.CONTINUE;
  }

  @Override
  public TraversalControl visitPluginPragma(PluginPragma pp, TraverserContext<Node<?, ?>> data) {
    return TraversalControl.CONTINUE;
  }

  @Override
  public TraversalControl visitOptionPragma(OptionPragma op, TraverserContext<Node<?, ?>> data) {
    return TraversalControl.CONTINUE;
  }

  @Override
  public TraversalControl visitCloseDirective(
      CloseDirective cd, TraverserContext<Node<?, ?>> data) {
    return TraversalControl.CONTINUE;
  }

  @Override
  public TraversalControl visitBalanceDirective(
      BalanceDirective bd, TraverserContext<Node<?, ?>> data) {
    return TraversalControl.CONTINUE;
  }

  @Override
  public TraversalControl visitCommodityDirective(
      CommodityDirective cd, TraverserContext<Node<?, ?>> data) {
    return TraversalControl.CONTINUE;
  }

  @Override
  public TraversalControl visitCustomDirective(
      CustomDirective cd, TraverserContext<Node<?, ?>> data) {
    return TraversalControl.CONTINUE;
  }

  @Override
  public TraversalControl visitDocumentDirective(
      DocumentDirective dd, TraverserContext<Node<?, ?>> data) {
    return TraversalControl.CONTINUE;
  }

  @Override
  public TraversalControl visitEventDirective(
      EventDirective ed, TraverserContext<Node<?, ?>> data) {
    return TraversalControl.CONTINUE;
  }

  @Override
  public TraversalControl visitNoteDirective(NoteDirective nd, TraverserContext<Node<?, ?>> data) {
    return TraversalControl.CONTINUE;
  }

  @Override
  public TraversalControl visitOpenDirective(OpenDirective od, TraverserContext<Node<?, ?>> data) {
    return TraversalControl.CONTINUE;
  }

  @Override
  public TraversalControl visitQueryDirective(
      QueryDirective qd, TraverserContext<Node<?, ?>> data) {
    return TraversalControl.CONTINUE;
  }

  @Override
  public TraversalControl visitTransactionDirective(
      TransactionDirective td, TraverserContext<Node<?, ?>> data) {
    return TraversalControl.CONTINUE;
  }

  @Override
  public TraversalControl visitPriceDirective(
      PriceDirective pd, TraverserContext<Node<?, ?>> data) {
    return TraversalControl.CONTINUE;
  }

  @Override
  public TraversalControl visitPadDirective(PadDirective pd, TraverserContext<Node<?, ?>> data) {
    return TraversalControl.CONTINUE;
  }

  @Override
  public TraversalControl visitJournal(Journal journal, TraverserContext<Node<?, ?>> data) {
    return TraversalControl.CONTINUE;
  }

  @Override
  public TraversalControl visitComment(Comment comment, TraverserContext<Node<?, ?>> data) {
    return TraversalControl.CONTINUE;
  }

  @Override
  public TraversalControl visitPosting(Posting posting, TraverserContext<Node<?, ?>> data) {
    return TraversalControl.CONTINUE;
  }
}
