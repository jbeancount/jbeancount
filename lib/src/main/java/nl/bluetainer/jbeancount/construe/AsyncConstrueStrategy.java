package nl.bluetainer.jbeancount.construe;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import nl.bluetainer.jbeancount.language.Journal;

public class AsyncConstrueStrategy implements BeancountConstrueStrategy {
  @Override
  public CompletableFuture<Journal> construe(Supplier<Journal> journalSupplier) {
    return CompletableFuture.supplyAsync(journalSupplier);
  }
}
