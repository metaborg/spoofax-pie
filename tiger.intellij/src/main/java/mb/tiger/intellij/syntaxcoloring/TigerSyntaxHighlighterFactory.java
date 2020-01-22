package mb.tiger.intellij.syntaxcoloring;

import mb.spoofax.intellij.SpoofaxPlugin;
import mb.spoofax.intellij.editor.SpoofaxSyntaxHighlighterFactory;
import mb.tiger.intellij.TigerPlugin;

public class TigerSyntaxHighlighterFactory extends SpoofaxSyntaxHighlighterFactory {
    // Instantiated by IntelliJ.
    private TigerSyntaxHighlighterFactory() {
        super(
            SpoofaxPlugin.getComponent().getResourceRegistry(),
            TigerPlugin.getComponent().getLexerFactory(),
            TigerPlugin.getComponent().getHighlighterFactory()
        );
    }
}
