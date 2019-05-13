package mb.tiger.intellij.syntaxcoloring;

import mb.spoofax.intellij.editor.SpoofaxLexer;
import mb.spoofax.intellij.editor.SpoofaxSyntaxHighlighter;
import mb.spoofax.intellij.editor.SpoofaxSyntaxHighlighterFactory;
import mb.spoofax.intellij.resource.IntellijResourceRegistry;
import mb.tiger.intellij.TigerPlugin;

public class TigerSyntaxHighlighterFactory extends SpoofaxSyntaxHighlighterFactory {
    public TigerSyntaxHighlighterFactory() {
        super(
            TigerPlugin.getComponent(),
            TigerPlugin.getComponent().getResourceRegistry(),
            TigerPlugin.getComponent().getLexerFactory(),
            TigerPlugin.getComponent().getHighlighterFactory()
        );
    }
}
