package nl.bluetainer.jbeancount.construe;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import nl.bluetainer.jbeancount.language.Journal;

public interface BeancountConstrueStrategy {

  CompletableFuture<Journal> construe(Supplier<Journal> journalSupplier);
}
