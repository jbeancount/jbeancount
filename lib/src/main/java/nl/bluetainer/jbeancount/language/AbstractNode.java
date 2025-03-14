package nl.bluetainer.jbeancount.language;

import java.util.Objects;
import nl.bluetainer.jbeancount.language.tools.NodeUtil;
import nl.bluetainer.jbeancount.language.tools.internal.NodeChildrenContainer;

abstract sealed class AbstractNode<T extends Node<T, B>, B extends AbstractNode.Builder<T, B>>
    implements Node<T, B>
    permits AbstractDirectiveNode, AbstractPragmaNode, Comment, Eol, Journal, Posting {

  private final SourceLocation sourceLocation;

  protected AbstractNode(SourceLocation sourceLocation) {
    this.sourceLocation = Objects.requireNonNull(sourceLocation, "sourceLocation");
  }

  @Override
  public SourceLocation sourceLocation() {
    return sourceLocation;
  }

  @Override
  public NodeChildrenContainer getNamedChildren() {
    return NodeChildrenContainer.newNodeChildrenContainer().build();
  }

  @SuppressWarnings("unchecked")
  @Override
  public T withNewChildren(NodeChildrenContainer newChildren) {
    NodeUtil.assertNewChildrenAreEmpty(newChildren);
    return (T) this;
  }

  public abstract static sealed class Builder<
          T extends Node<T, B>, B extends AbstractNode.Builder<T, B>>
      implements Node.Builder<T, B>
      permits AbstractDirectiveNode.Builder,
          AbstractPragmaNode.Builder,
          Comment.Builder,
          Eol.Builder,
          Journal.Builder,
          Posting.Builder {
    Builder() {}

    Builder(SourceLocation sourceLocation) {
      this.sourceLocation = sourceLocation;
    }

    private SourceLocation sourceLocation;

    public SourceLocation sourceLocation() {
      return sourceLocation;
    }

    @SuppressWarnings("unchecked")
    public B sourceLocation(SourceLocation sourceLocation) {
      this.sourceLocation = sourceLocation;
      return (B) this;
    }
  }
}
