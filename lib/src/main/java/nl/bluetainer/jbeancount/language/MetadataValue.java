package nl.bluetainer.jbeancount.language;

public sealed interface MetadataValue permits Amount, ScalarValue {

  default boolean empty() {
    return false;
  }
}
