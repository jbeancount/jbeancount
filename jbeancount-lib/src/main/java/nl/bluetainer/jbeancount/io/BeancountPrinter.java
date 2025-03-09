package nl.bluetainer.jbeancount.io;

import nl.bluetainer.jbeancount.language.Journal;
import org.jetbrains.annotations.NotNull;

public interface BeancountPrinter {

  @NotNull
  String print(@NotNull Journal journal) throws BeancountIOException;
}
