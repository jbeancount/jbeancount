package nl.bluetainer.jbeancount.cli.commands;

import nl.bluetainer.jbeancount.cli.commands.jordie.JordieCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
    name = "internal",
    description = "Internal commands to debug the utility and library",
    subcommands = {LexCommand.class, JordieCommand.class},
    scope = CommandLine.ScopeType.INHERIT)
public class InternalCommand {}
