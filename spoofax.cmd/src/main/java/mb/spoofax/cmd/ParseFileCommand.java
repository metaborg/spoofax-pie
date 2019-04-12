package mb.spoofax.cmd;

import mb.common.message.MessageCollection;
import mb.pie.api.None;
import mb.pie.api.Pie;
import mb.pie.api.exec.TopDownSession;
import mb.resource.fs.FSPath;
import mb.spoofax.core.language.AstResult;
import mb.spoofax.core.language.LanguageInstance;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(description = "Parses a file and shows the AST (if successful) and messages (if any)")
public class ParseFileCommand implements Callable<None> {
    private final LanguageInstance languageInstance;
    private final Pie pie;

    public ParseFileCommand(LanguageInstance languageInstance, Pie pie) {
        this.languageInstance = languageInstance;
        this.pie = pie;
    }

    @SuppressWarnings("NullableProblems") @Parameters(index = "0", description = "Path to file to parse")
    private Path filePath;

    @Override public None call() throws Exception {
        final FSPath filePath = new FSPath(this.filePath);
        final TopDownSession session = pie.getTopDownExecutor().newSession();
        final AstResult astResult = session.requireInitial(languageInstance.createAstTask(filePath));
        if(astResult.ast != null) {
            System.out.println(astResult.ast.toString());
        }
        final MessageCollection messages = session.requireInitial(languageInstance.createMessagesTask(filePath));
        System.out.println(messages);
        return None.instance;
    }
}
