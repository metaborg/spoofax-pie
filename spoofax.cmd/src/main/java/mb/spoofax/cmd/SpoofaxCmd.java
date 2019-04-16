package mb.spoofax.cmd;

import mb.resource.string.StringResourceRegistry;
import mb.spoofax.core.language.LanguageComponent;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.RunAll;

import javax.inject.Inject;
import java.util.concurrent.Callable;

@Command
public class SpoofaxCmd implements Callable<Void> {
    private final StringResourceRegistry stringResourceRegistry;

    @Inject
    public SpoofaxCmd(StringResourceRegistry stringResourceRegistry) {
        this.stringResourceRegistry = stringResourceRegistry;
    }

    public void run(String[] args, LanguageComponent languageComponent) {
        CommandLine commandLine = new CommandLine(this);
        commandLine.addSubcommand("parse", new ParseFileCommand(languageComponent));
        commandLine.addSubcommand("parse-string",
            new ParseStringCommand(stringResourceRegistry, languageComponent));
        commandLine.parseWithHandler(new RunAll(), args);
    }

    @Override public Void call() {
        return null;
    }
}
