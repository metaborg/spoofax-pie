package mb.spoofax.cmd;

import mb.pie.api.Pie;
import mb.resource.string.StringResourceRegistry;
import mb.spoofax.core.language.LanguageInstance;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.RunAll;

import javax.inject.Inject;
import java.util.concurrent.Callable;

@Command
public class SpoofaxCmd implements Callable<Void> {
    private final StringResourceRegistry stringResourceRegistry;
    private final LanguageInstance languageInstance;
    private final Pie pie;

    @Inject
    public SpoofaxCmd(StringResourceRegistry stringResourceRegistry, LanguageInstance languageInstance, Pie pie) {
        this.stringResourceRegistry = stringResourceRegistry;
        this.languageInstance = languageInstance;
        this.pie = pie;
    }

    public void run(String[] args) {
        CommandLine commandLine = new CommandLine(this);
        commandLine.addSubcommand("parse", new ParseFileCommand(languageInstance, pie));
        commandLine.addSubcommand("parse-string",
            new ParseStringCommand(stringResourceRegistry, languageInstance, pie));
        commandLine.parseWithHandler(new RunAll(), args);
    }

    @Override public Void call() throws Exception {
        return null;
    }
}
