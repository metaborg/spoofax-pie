package mb.tiger.intellij.syntaxcoloring;

import mb.spoofax.intellij.editor.SpoofaxParserDefinition;
import mb.tiger.intellij.TigerPlugin;

public class TigerParserDefinition extends SpoofaxParserDefinition {
    // Instantiated by IntelliJ.
    private TigerParserDefinition() {
        super(TigerPlugin.getComponent());
    }
}
