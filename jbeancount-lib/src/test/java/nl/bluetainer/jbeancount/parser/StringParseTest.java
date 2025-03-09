package nl.bluetainer.jbeancount.parser;

import static nl.bluetainer.jbeancount.parser.antlr.BeancountAntlrLexer.STRING;
import static nl.bluetainer.jbeancount.testing.TestUtil.assertToLanguage;
import static nl.bluetainer.jbeancount.testing.TestUtil.t;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class StringParseTest {

  @ParameterizedTest
  @ValueSource(
      strings = {"", "1", "a", "1b1", "c2c", "\\\"a\\\"", "\\\"", "\\n", "\\t\\n\\r\\f\\b"})
  public void assertToLanguageStringParses(String theString) {
    final String tokenText = '"' + theString + '"';
    assertToLanguage(antlr -> antlr.parseStringToken(t(STRING, tokenText))).isEqualTo(theString);
  }
}
