package nl.bluetainer.jbeancount.lexer;

import static nl.bluetainer.jbeancount.parser.antlr.BeancountAntlrParser.STRING;
import static nl.bluetainer.jbeancount.testing.TestUtil.assertLexedTokensEquals;
import static nl.bluetainer.jbeancount.testing.TestUtil.et;

import org.junit.jupiter.api.Test;

public class StringLexTest {

  @Test
  public void emptyString() {
    assertLexedTokensEquals("\"\"", et(STRING, "\"\"", 1, 0));
  }

  @Test
  public void someText() {
    assertLexedTokensEquals("\"abc123\"", et(STRING, "\"abc123\"", 1, 0));
  }

  @Test
  public void textWithNewlines() {
    assertLexedTokensEquals("\"a\nb\"", et(STRING, "\"a\nb\"", 1, 0));
  }

  @Test
  public void escapeOneQuote() {
    assertLexedTokensEquals("\"a\\\"b\"", et(STRING, "\"a\\\"b\"", 1, 0));
  }

  @Test
  public void escapeTwoQuotes() {
    assertLexedTokensEquals("\"a\\\"c\\\"b\"", et(STRING, "\"a\\\"c\\\"b\"", 1, 0));
  }
}
