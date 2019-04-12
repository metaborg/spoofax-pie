package mb.spoofax.cmd;

import mb.common.message.MessageCollection;
import mb.pie.api.None;
import mb.pie.api.Pie;
import mb.pie.api.exec.TopDownSession;
import mb.resource.string.StringResource;
import mb.resource.string.StringResourceRegistry;
import mb.spoofax.core.language.AstResult;
import mb.spoofax.core.language.LanguageInstance;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

@Command(description = "Parses a string and shows the AST (if successful) and messages (if any)")
public class ParseStringCommand implements Callable<None> {
    private final StringResourceRegistry stringResourceRegistry;
    private final LanguageInstance languageInstance;
    private final Pie pie;

    public ParseStringCommand(StringResourceRegistry stringResourceRegistry, LanguageInstance languageInstance, Pie pie) {
        this.stringResourceRegistry = stringResourceRegistry;
        this.languageInstance = languageInstance;
        this.pie = pie;
    }

    @SuppressWarnings("NullableProblems") @Parameters(index = "0", description = "String to parse")
    private String stringToParse;

    @Override public None call() throws Exception {
        final StringResource stringResource = new StringResource(stringToParse, 0);
        stringResourceRegistry.addResource(stringResource);
        final TopDownSession session = pie.getTopDownExecutor().newSession();
        final AstResult astResult = session.requireInitial(languageInstance.createAstTask(stringResource.key));
        if(astResult.ast != null) {
            System.out.println(astResult.ast.toString());
        }
        final MessageCollection messages =
            session.requireInitial(languageInstance.createMessagesTask(stringResource.key));
        System.out.println(messages);
        return None.instance;
    }
}
