package nl.bluetainer.jbeancount.cli;

import java.nio.file.Path;

import nl.bluetainer.jbeancount.cli.commands.CheckJournal;
import nl.bluetainer.jbeancount.cli.commands.FormatJournal;
import nl.bluetainer.jbeancount.cli.commands.IncludeTreeCommand;
import nl.bluetainer.jbeancount.cli.commands.InternalCommand;
import nl.bluetainer.jbeancount.cli.commands.MergeJournal;
import nl.bluetainer.jbeancount.cli.commands.SortJournal;
import nl.bluetainer.jbeancount.cli.picocli.BeancountExecutionExceptionHandler;
import nl.bluetainer.jbeancount.cli.picocli.PathConverter;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help;

@Command(
    name = "jbeancount",
    mixinStandardHelpOptions = true,
    version = {
      "JBeancount %1$s",
      "Java ${java.version} by ${java.vendor} (${java.vm.name}, ${java.vm.version})"
    },
    description = "Extension utilities for the Beancount plain text accounting tool",
    subcommands = {
      MergeJournal.class,
      FormatJournal.class,
      CheckJournal.class,
      IncludeTreeCommand.class,
      InternalCommand.class,
      SortJournal.class
    },
    scope = CommandLine.ScopeType.INHERIT)
public final class BeancountCli {

  private static final String VERSION = System.getProperty("jbeancount.version", "DEVELOPMENT BUILD");

  private BeancountCli() {}


  public static void main(String... args) {
    @SuppressWarnings("InstantiationOfUtilityClass")
    final CommandLine commandLine = new CommandLine(new BeancountCli());
    commandLine.registerConverter(Path.class, new PathConverter());
    commandLine.setExecutionExceptionHandler(new BeancountExecutionExceptionHandler());

    // To parse version flag, maybe implement different behaviour in the future
    commandLine.parseArgs(args);
    if (commandLine.isVersionHelpRequested()) {
      commandLine.printVersionHelp(System.out, Help.Ansi.AUTO, BeancountCli.VERSION);
      return;
    }

    // Otherwise, execute the command
    final int exitCode = commandLine.execute(args);
    System.exit(exitCode);
  }
}
