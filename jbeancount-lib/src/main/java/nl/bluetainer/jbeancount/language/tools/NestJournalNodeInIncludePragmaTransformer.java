package nl.bluetainer.jbeancount.language.tools;

import graphql.util.TraversalControl;
import graphql.util.TraverserContext;
import graphql.util.TreeTransformerUtil;
import java.util.Map;
import java.util.Objects;
import nl.bluetainer.jbeancount.language.IncludePragma;
import nl.bluetainer.jbeancount.language.Journal;
import nl.bluetainer.jbeancount.language.Node;

public final class NestJournalNodeInIncludePragmaTransformer {

  private NestJournalNodeInIncludePragmaTransformer() {}

  public static Journal transform(
      Journal journal, Map<IncludePragma, Journal> includePragmaJournalMap) {
    if (includePragmaJournalMap.isEmpty()) {
      return journal;
    }
    return (Journal)
        AstTransformer.transform(journal, new IncludeJournalTransformer(includePragmaJournalMap));
  }

  private static final class IncludeJournalTransformer extends NodeVisitorStub {

    private final Map<IncludePragma, Journal> pragmaToJournalMapping;

    public IncludeJournalTransformer(Map<IncludePragma, Journal> pragmaToJournalMapping) {
      this.pragmaToJournalMapping =
          Objects.requireNonNull(pragmaToJournalMapping, "pragmaToJournalMapping");
    }

    @Override
    public TraversalControl visitIncludePragma(
        IncludePragma ip, TraverserContext<Node<?, ?>> data) {
      final Journal journal = pragmaToJournalMapping.get(ip);
      if (journal == null) {
        return TraversalControl.CONTINUE;
      }
      final IncludePragma pragmaWithJournal = ip.transform(builder -> builder.journal(journal));
      return TreeTransformerUtil.changeNode(data, pragmaWithJournal);
    }
  }
}
