package nl.bluetainer.jbeancount.cli.commands;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import nl.bluetainer.jbeancount.Beancount;
import nl.bluetainer.jbeancount.cli.commands.mixin.SingleOutput;
import nl.bluetainer.jbeancount.cli.internal.transformations.SortTransactions;
import nl.bluetainer.jbeancount.io.SimpleBeancountPrinter;
import nl.bluetainer.jbeancount.language.CustomDirective;
import nl.bluetainer.jbeancount.language.Journal;
import nl.bluetainer.jbeancount.language.JournalDeclaration;
import nl.bluetainer.jbeancount.language.StringValue;
import nl.bluetainer.jbeancount.language.TransactionDirective;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Parameters;

@Command(name = "sort", description = "This command sorts the transactions in a Beancount file")
public class SortJournal implements Callable<Integer> {

  @Parameters(index = "0", description = "The Beancount file")
  private Path file;

  @Mixin private SingleOutput output;

  @Override
  public Integer call() throws Exception {
    Beancount beancount = Beancount.newBeancount().build();
    Journal journal = beancount.createJournalSyncWithoutIncludes(file);
    for (JournalDeclaration<?, ?> declaration : journal.declarations()) {
      if (!(declaration instanceof TransactionDirective)
          && !(declaration instanceof CustomDirective cd
              && "fava-option".equals(cd.name())
              && cd.values().size() == 2
              && ((StringValue) cd.values().get(0)).value().equals("insert-entry"))) {
        throw new Exception(
            "The sort command can currently only handle files with transaction directives, and Fava custom insert-entry directives");
      }
    }
    journal = SortTransactions.sortTransactions(journal);
    SimpleBeancountPrinter beancountPrinter = SimpleBeancountPrinter.newDefaultPrinter();
    final String journalAsString = beancountPrinter.print(journal);
    if (output.hasOutput()) {
      Files.writeString(output.outputFile(), journalAsString, StandardCharsets.UTF_8);
    } else {
      System.out.println(journalAsString);
    }
    return 0;
  }
}
