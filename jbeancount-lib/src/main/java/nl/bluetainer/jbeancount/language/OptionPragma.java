package nl.bluetainer.jbeancount.language;

import graphql.util.TraversalControl;
import graphql.util.TraverserContext;
import java.util.Objects;
import java.util.function.Consumer;
import nl.bluetainer.jbeancount.language.tools.NodeVisitor;

public final class OptionPragma extends AbstractPragmaNode<OptionPragma, OptionPragma.Builder> {

  private final String name;
  private final String value;

  private OptionPragma(SourceLocation sourceLocation, String name, String value, Comment comment) {
    super(sourceLocation, comment);
    this.name = Objects.requireNonNull(name, "name");
    this.value = Objects.requireNonNull(value, "value");
  }

  public String name() {
    return name;
  }

  public String value() {
    return value;
  }

  public static Builder newOptionPragma() {
    return new Builder();
  }

  @Override
  public TraversalControl accept(TraverserContext<Node<?, ?>> context, NodeVisitor visitor) {
    return visitor.visitOptionPragma(this, context);
  }

  @Override
  public OptionPragma transform(Consumer<Builder> builderConsumer) {
    final Builder b = new Builder(sourceLocation(), name, value, comment());
    builderConsumer.accept(b);
    return b.build();
  }

  public static final class Builder extends AbstractPragmaNode.Builder<OptionPragma, Builder> {
    private String name;
    private String value;

    private Builder() {}

    private Builder(SourceLocation sourceLocation, String name, String value, Comment comment) {
      super(sourceLocation, comment);
      this.name = name;
      this.value = value;
    }

    @Override
    public OptionPragma build() {
      return new OptionPragma(sourceLocation(), name, value, comment());
    }

    public String name() {
      return name;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public String value() {
      return value;
    }

    public Builder value(String value) {
      this.value = value;
      return this;
    }
  }
}
