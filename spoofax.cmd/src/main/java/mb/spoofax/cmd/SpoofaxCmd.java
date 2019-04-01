package mb.spoofax.cmd;

import mb.pie.api.Pie;
import mb.spoofax.core.language.LanguageComponent;
import picocli.CommandLine;

public class SpoofaxCmd {
    private final LanguageComponent languageComponent;
    private final Pie pie;

    public SpoofaxCmd(LanguageComponent languageComponent, Pie pie) {
        this.languageComponent = languageComponent;
        this.pie = pie;
    }

    public void run(String[] args) {
        CommandLine.call(new ParseCommand(languageComponent, pie), args);
    }
}
