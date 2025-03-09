package nl.bluetainer.jbeancount.language;

public sealed interface Link extends TagOrLink, MetadataLine permits LinkValue {

  String link();
}
