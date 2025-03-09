package nl.bluetainer.jbeancount.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;
import nl.bluetainer.jbeancount.language.Journal;
import nl.bluetainer.jbeancount.language.JournalDeclaration;
import nl.bluetainer.jbeancount.language.Metadata;
import nl.bluetainer.jbeancount.language.MetadataItem;
import nl.bluetainer.jbeancount.language.MetadataKey;
import nl.bluetainer.jbeancount.language.MetadataLine;
import nl.bluetainer.jbeancount.language.MetadataValue;
import nl.bluetainer.jbeancount.language.StringValue;

public final class BeancountUtil {

  private BeancountUtil() {}

  public static <T extends JournalDeclaration<?, ?>> List<T> findDeclarationsOfType(
      Journal journal, Class<T> type) {
    return journal.declarations().stream().filter(type::isInstance).map(type::cast).toList();
  }

  public static List<JournalDeclaration<?, ?>> findDeclarations(
      Journal journal, Predicate<JournalDeclaration<?, ?>> declarationPredicate) {
    return journalDeclarationStream(journal, declarationPredicate).toList();
  }

  public static <T extends JournalDeclaration<?, ?>> List<T> findDeclarations(
      Journal journal, Class<T> type, Predicate<T> declarationPredicate) {
    return journal.declarations().stream()
        .<T>mapMulti(
            (journalDeclaration, consumer) -> {
              if (type.isInstance(journalDeclaration)) {
                T t = type.cast(journalDeclaration);
                if (declarationPredicate.test(t)) {
                  consumer.accept(t);
                }
              }
            })
        .toList();
  }

  private static Stream<JournalDeclaration<?, ?>> journalDeclarationStream(
      Journal journal, Predicate<JournalDeclaration<?, ?>> declarationPredicate) {
    return journal.declarations().stream().filter(declarationPredicate);
  }

  public static boolean hasMetadataWithKey(Metadata metadata, String key) {
    Objects.requireNonNull(metadata, "metadata");
    Objects.requireNonNull(key, "key");
    return metadata.metadata().stream()
        .anyMatch(ml -> ml instanceof MetadataItem mi && mi.key().key().equals(key));
  }

  public static Metadata addMetadataAtStart(Metadata metadata, MetadataLine... linesToAdd) {
    return metadata.transform(
        builder -> {
          List<MetadataLine> metadataLines = new ArrayList<>(Arrays.asList(linesToAdd));
          metadataLines.addAll(builder.metadata());
          builder.metadata(metadataLines);
        });
  }

  public static Metadata addMetadataAtEnd(Metadata metadata, MetadataLine... linesToAdd) {
    return metadata.transform(
        builder -> {
          List<MetadataLine> metadataLines = builder.metadata();
          metadataLines = new ArrayList<>(metadataLines);
          metadataLines.addAll(Arrays.asList(linesToAdd));
          builder.metadata(metadataLines);
        });
  }

  public static boolean withNewMetadataLine(Metadata metadata, String key) {
    Objects.requireNonNull(metadata, "metadata");
    Objects.requireNonNull(key, "key");
    return metadata.metadata().stream()
        .anyMatch(ml -> ml instanceof MetadataItem mi && mi.key().key().equals(key));
  }

  public static MetadataItem newMetadataItem(String key, String value) {
    return MetadataItem.newMetadataItem()
        .key(MetadataKey.newMetadataKey().key(key).build())
        .value(StringValue.newStringValue().value(value).build())
        .build();
  }

  public static MetadataValue getMetadataValue(Metadata metadata, String key) {
    Objects.requireNonNull(metadata, "metadata");
    Objects.requireNonNull(key, "key");
    return metadata.metadata().stream()
        .filter(ml -> ml instanceof MetadataItem mi && mi.key().key().equals(key))
        .findFirst()
        .map(metadataLine -> ((MetadataItem) metadataLine).value())
        .orElse(null);
  }
}
