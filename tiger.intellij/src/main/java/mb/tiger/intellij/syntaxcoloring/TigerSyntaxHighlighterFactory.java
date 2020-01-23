package mb.tiger.intellij.syntaxcoloring;

import mb.spoofax.intellij.editor.SpoofaxSyntaxHighlighterFactory;
import mb.tiger.intellij.TigerPlugin;

public class TigerSyntaxHighlighterFactory extends SpoofaxSyntaxHighlighterFactory {
    // Instantiated by IntelliJ.
    private TigerSyntaxHighlighterFactory() {
        super(TigerPlugin.getComponent());
    }
}
