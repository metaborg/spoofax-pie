package mb.spoofax.cli;

import mb.resource.string.StringResourceRegistry;
import mb.spoofax.core.language.LanguageComponent;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.RunAll;

import javax.inject.Inject;
import java.util.concurrent.Callable;

@Command
public class SpoofaxCli implements Callable<Void> {
    private final StringResourceRegistry stringResourceRegistry;

    @Inject
    public SpoofaxCli(StringResourceRegistry stringResourceRegistry) {
        this.stringResourceRegistry = stringResourceRegistry;
    }

    public void run(String[] args, LanguageComponent languageComponent) {
        CommandLine commandLine = new CommandLine(this);
        commandLine.parseWithHandler(new RunAll(), args);
    }

    @Override public Void call() {
        return null;
    }
}
