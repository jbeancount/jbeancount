package nl.bluetainer.jbeancount.io;

import nl.bluetainer.jbeancount.BeancountException;

public class BeancountIOException extends BeancountException {

  public BeancountIOException() {}

  public BeancountIOException(String message) {
    super(message);
  }
}
