package nl.bluetainer.jbeancount.cli.commands;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import nl.bluetainer.jbeancount.Beancount;
import nl.bluetainer.jbeancount.BeancountInvalidStateException;
import nl.bluetainer.jbeancount.cli.commands.mixin.SingleOutput;
import nl.bluetainer.jbeancount.cli.internal.transformations.FlattenJournal;
import nl.bluetainer.jbeancount.io.SimpleBeancountPrinter;
import nl.bluetainer.jbeancount.language.Journal;
import nl.bluetainer.jbeancount.language.JournalDeclaration;
import nl.bluetainer.jbeancount.language.SourceLocation;
import nl.bluetainer.jbeancount.util.ImmutableKit;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
    name = "merge",
    description =
        "This command (recursively) merges all journals included using include pragmas, and creates one composite journal")
public class MergeJournal implements Callable<Integer> {

  @Parameters(
      index = "0",
      arity = "1..*",
      description = "The Beancount file(s) at the root(s) of your inclusion tree(s)")
  private List<Path> files;

  @Option(
      names = "-r",
      description =
          "Recursively aggregate (also flattens include pragmas within included journals)",
      defaultValue = "false")
  private boolean recurse;

  @Option(
      names = "--keep-include-pragmas",
      description =
          "Keep the include pragmas present in the composite journal (just before the contents of said journal)",
      defaultValue = "false")
  private boolean keepIncludePragmas;

  @Mixin private SingleOutput output;

  @Override
  public Integer call() throws IOException {
    Beancount beancount = Beancount.newBeancount().build();
    if (files.isEmpty()) {
      throw new BeancountInvalidStateException();
    }
    final Journal masterJournal =
        files.stream()
            .map(beancount::createJournalSync)
            .reduce(
                Journal.newJournal()
                    .declarations(ImmutableKit.emptyList())
                    .sourceLocation(SourceLocation.EMPTY)
                    .build(),
                (journal, journal2) ->
                    FlattenJournal.flattenJournal(journal, recurse, keepIncludePragmas)
                        .transform(
                            builder -> {
                              List<JournalDeclaration<?, ?>> combinedDeclarations =
                                  new ArrayList<>(builder.declarations());
                              combinedDeclarations.addAll(
                                  FlattenJournal.flattenJournal(
                                          journal2, recurse, keepIncludePragmas)
                                      .declarations());
                              builder.declarations(combinedDeclarations);
                            }));
    SimpleBeancountPrinter beancountPrinter = SimpleBeancountPrinter.newDefaultPrinter();
    final String journalAsString = beancountPrinter.print(masterJournal);
    if (output.hasOutput()) {
      Files.writeString(output.outputFile(), journalAsString, StandardCharsets.UTF_8);
    } else {
      System.out.println(journalAsString);
    }
    return 0;
  }
}
