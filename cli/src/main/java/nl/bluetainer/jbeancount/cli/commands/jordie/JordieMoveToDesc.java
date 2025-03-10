package nl.bluetainer.jbeancount.cli.commands.jordie;

import graphql.util.TraversalControl;
import graphql.util.TraverserContext;
import graphql.util.TreeTransformerUtil;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import nl.bluetainer.jbeancount.Beancount;
import nl.bluetainer.jbeancount.cli.commands.mixin.SingleOutput;
import nl.bluetainer.jbeancount.io.SimpleBeancountPrinter;
import nl.bluetainer.jbeancount.language.Journal;
import nl.bluetainer.jbeancount.language.Node;
import nl.bluetainer.jbeancount.language.TransactionDirective;
import nl.bluetainer.jbeancount.language.tools.AstTransformer;
import nl.bluetainer.jbeancount.language.tools.NodeVisitorStub;
import nl.bluetainer.jbeancount.parser.BeancountUtil;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ScopeType;

@Command(name = "movetodesc", scope = ScopeType.LOCAL)
public class JordieMoveToDesc implements Callable<Integer> {

  @Parameters(index = "0", description = "The Beancount file")
  private Path file;

  @Mixin private SingleOutput output;

  @Override
  public Integer call() throws Exception {
    Beancount beancount = Beancount.newBeancount().build();
    Journal journal = beancount.createJournalSyncWithoutIncludes(file);
    journal =
        (Journal)
            AstTransformer.transform(
                journal,
                new NodeVisitorStub() {
                  @Override
                  public TraversalControl visitTransactionDirective(
                      TransactionDirective td, TraverserContext<Node<?, ?>> data) {
                    if (!BeancountUtil.hasMetadataWithKey(td.metadata(), "desc")) {
                      TransactionDirective newtd =
                          td.transform(
                              transactionDirectiveBuilder ->
                                  transactionDirectiveBuilder
                                      .narration(null)
                                      .metadata(
                                          BeancountUtil.addMetadataAtStart(
                                              transactionDirectiveBuilder.metadata(),
                                              BeancountUtil.newMetadataItem(
                                                  "desc", td.narration()))));
                      return TreeTransformerUtil.changeNode(data, newtd);
                    }
                    return TraversalControl.CONTINUE;
                  }
                });

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
