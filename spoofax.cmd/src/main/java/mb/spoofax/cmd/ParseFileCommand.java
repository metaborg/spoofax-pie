package mb.spoofax.cmd;

import mb.common.message.MessageCollection;
import mb.pie.api.None;
import mb.pie.api.PieSession;
import mb.resource.fs.FSPath;
import mb.spoofax.core.language.AstResult;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.core.language.LanguageInstance;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(description = "Parses a file and shows the AST (if successful) and messages (if any)")
public class ParseFileCommand implements Callable<None> {
    private final LanguageComponent languageComponent;

    public ParseFileCommand(LanguageComponent languageComponent) {
        this.languageComponent = languageComponent;
    }

    @SuppressWarnings("NullableProblems") @Parameters(index = "0", description = "Path to file to parse")
    private Path filePath;

    @Override public None call() throws Exception {
        final LanguageInstance languageInstance = languageComponent.getLanguageInstance();
        final FSPath filePath = new FSPath(this.filePath);
        try(final PieSession session = languageComponent.newPieSession()) {
            final AstResult astResult = session.requireTopDown(languageInstance.createAstTask(filePath));
            if(astResult.ast != null) {
                System.out.println(astResult.ast.toString());
            }
            final MessageCollection messages = session.requireTopDown(languageInstance.createMessagesTask(filePath));
            System.out.println(messages);
            return None.instance;
        }
    }
}
