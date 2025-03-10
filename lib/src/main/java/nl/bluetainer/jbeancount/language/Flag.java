package nl.bluetainer.jbeancount.language;

public sealed interface Flag permits SymbolFlag, TxnFlag {

  String flag();

  boolean star();

  boolean exclamationMark();

  boolean txn();
}
