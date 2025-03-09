package nl.bluetainer.jbeancount.parser;

import java.util.Objects;
import nl.bluetainer.jbeancount.language.SourceLocation;

public class InvalidSyntaxException extends RuntimeException {

  private final SourceLocation sourceLocation;
  private final String preview;

  public InvalidSyntaxException(SourceLocation sourceLocation, String message, String preview) {
    super(message);
    this.sourceLocation = Objects.requireNonNull(sourceLocation, "sourceLocation");
    this.preview = Objects.requireNonNull(preview, "preview");
  }

  public SourceLocation getSourceLocation() {
    return sourceLocation;
  }

  public String getPreview() {
    return preview;
  }
}
