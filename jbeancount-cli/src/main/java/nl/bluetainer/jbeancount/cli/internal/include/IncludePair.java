package nl.bluetainer.jbeancount.cli.internal.include;

import java.nio.file.Path;

public record IncludePair(Path fromJournal, Path toJournal) {}
