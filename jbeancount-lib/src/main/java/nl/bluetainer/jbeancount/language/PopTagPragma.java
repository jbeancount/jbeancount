package nl.bluetainer.jbeancount.language;

import graphql.util.TraversalControl;
import graphql.util.TraverserContext;
import java.util.function.Consumer;
import nl.bluetainer.jbeancount.language.tools.NodeVisitor;

public final class PopTagPragma extends AbstractPragmaNode<PopTagPragma, PopTagPragma.Builder> {
  PopTagPragma(SourceLocation sourceLocation, Comment comment) {
    super(sourceLocation, comment);
  }

  @Override
  public TraversalControl accept(TraverserContext<Node<?, ?>> context, NodeVisitor visitor) {
    return null;
  }

  @Override
  public PopTagPragma transform(Consumer<Builder> builderConsumer) {
    return null;
  }

  public static final class Builder extends AbstractPragmaNode.Builder<PopTagPragma, Builder> {
    @Override
    public PopTagPragma build() {
      return null;
    }
  }
}
