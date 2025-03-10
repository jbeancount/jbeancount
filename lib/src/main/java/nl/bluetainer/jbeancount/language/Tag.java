package nl.bluetainer.jbeancount.language;

public sealed interface Tag extends TagOrLink, MetadataLine permits TagValue {

  String tag();
}
