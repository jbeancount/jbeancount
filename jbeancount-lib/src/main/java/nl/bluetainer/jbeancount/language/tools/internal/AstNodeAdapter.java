package nl.bluetainer.jbeancount.language.tools.internal;

import graphql.util.NodeAdapter;
import graphql.util.NodeLocation;
import java.util.List;
import java.util.Map;
import nl.bluetainer.jbeancount.language.Node;
import nl.bluetainer.jbeancount.language.tools.NodeUtil;

public class AstNodeAdapter implements NodeAdapter<Node<?, ?>> {

  public static final AstNodeAdapter AST_NODE_ADAPTER = new AstNodeAdapter();

  private AstNodeAdapter() {}

  @Override
  public Map<String, List<Node<?, ?>>> getNamedChildren(Node<?, ?> node) {
    return node.getNamedChildren().getChildren();
  }

  @Override
  public Node<?, ?> withNewChildren(Node<?, ?> node, Map<String, List<Node<?, ?>>> newChildren) {
    NodeChildrenContainer nodeChildrenContainer =
        NodeChildrenContainer.newNodeChildrenContainer(newChildren).build();
    return node.withNewChildren(nodeChildrenContainer);
  }

  @Override
  public Node<?, ?> removeChild(Node<?, ?> node, NodeLocation location) {
    return NodeUtil.removeChild(node, location);
  }
}
