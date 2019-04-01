package mb.spoofax.cmd;

import mb.common.message.MessageCollection;
import mb.fs.api.path.FSPath;
import mb.fs.java.JavaFSPath;
import mb.pie.api.Pie;
import mb.pie.api.exec.TopDownSession;
import mb.spoofax.core.language.LanguageComponent;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(name = "parse", description = "Parses a file and shows the AST (if successful) and messages (if any)")
public class ParseCommand implements Callable<Void> {
    private final LanguageComponent languageComponent;
    private final Pie pie;

    public ParseCommand(LanguageComponent languageComponent, Pie pie) {
        this.languageComponent = languageComponent;
        this.pie = pie;
    }


    @Parameters(index = "0", description = "File to parse")
    private Path fileToParse;


    @Override public Void call() throws Exception {
        final FSPath file = new JavaFSPath(fileToParse);
        final TopDownSession session = pie.getTopDownExecutor().newSession();
        final @Nullable IStrategoTerm ast = session.requireInitial(languageComponent.astTaskDef().createTask(file));
        if(ast != null) {
            System.out.println(ast.toString());
        }
        final @Nullable MessageCollection messages =
            session.requireInitial(languageComponent.messagesTaskDef().createTask(file));
        if(messages != null) {
            System.out.println(messages);
        }
        return null;
    }
}
