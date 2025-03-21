package nl.bluetainer.jbeancount.cli.internal.transformations;

import graphql.Assert;
import graphql.util.TraversalControl;
import graphql.util.TraverserContext;
import graphql.util.TreeTransformerUtil;
import java.util.HashSet;
import java.util.Set;
import nl.bluetainer.jbeancount.language.IncludePragma;
import nl.bluetainer.jbeancount.language.Journal;
import nl.bluetainer.jbeancount.language.JournalDeclaration;
import nl.bluetainer.jbeancount.language.Node;
import nl.bluetainer.jbeancount.language.tools.AstTransformer;
import nl.bluetainer.jbeancount.language.tools.NodeVisitor;
import nl.bluetainer.jbeancount.language.tools.NodeVisitorStub;

public final class FlattenJournal {

  private FlattenJournal() {}

  public static Journal flattenJournal(
      Journal journal, boolean recursively, boolean keepIncludePragmas) {
    final NodeVisitor nodeVisitor =
        new SingleRootJournalMultiUseFlatteningNodeVisitor(!keepIncludePragmas, recursively);
    return (Journal) AstTransformer.transform(journal, nodeVisitor);
  }

  private static final class SingleRootJournalMultiUseFlatteningNodeVisitor
      extends NodeVisitorStub {

    private final Set<String> resolvedPaths;
    private final boolean removeIncludePragma;
    private final boolean isRecursive;

    private SingleRootJournalMultiUseFlatteningNodeVisitor(
        boolean removeIncludePragma, boolean isRecursive) {
      this.removeIncludePragma = removeIncludePragma;
      this.isRecursive = isRecursive;
      this.resolvedPaths = new HashSet<>();
    }

    @Override
    public TraversalControl visitIncludePragma(
        IncludePragma ip, TraverserContext<Node<?, ?>> data) {
      if (resolvedPaths.contains(ip.filename())) {
        if (removeIncludePragma || !isRecursive) {
          Assert.assertShouldNeverHappen();
          return TraversalControl.QUIT;
        } else {
          return TraversalControl.CONTINUE;
        }
      }
      if (removeIncludePragma) {
        TreeTransformerUtil.deleteNode(data);
      }
      final Journal journal = ip.journal();
      if (journal != null) {
        resolvedPaths.add(ip.filename());
        for (JournalDeclaration<?, ?> declaration : journal.declarations()) {
          final TraversalControl traversalControl =
              TreeTransformerUtil.insertAfter(data, declaration);
          if (traversalControl != TraversalControl.CONTINUE) {
            return traversalControl;
          }
        }
      }
      return TraversalControl.CONTINUE;
    }
  }
}
