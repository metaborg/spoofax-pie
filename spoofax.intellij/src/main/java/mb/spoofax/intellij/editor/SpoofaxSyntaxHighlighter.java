package mb.spoofax.intellij.editor;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.intellij.psi.SpoofaxTokenType;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Highlighting lexer for Spoofax languages in IntelliJ.
 */
public final class SpoofaxSyntaxHighlighter extends SyntaxHighlighterBase {
    private final Lexer lexer;
    private final ScopeManager scopeManager;


    /**
     * Factory class for {@link SpoofaxSyntaxHighlighter}.
     */
    @LanguageScope
    public static class Factory {
        private final ScopeManager scopeManager;

        @Inject public Factory(ScopeManager scopeManager) {
            this.scopeManager = scopeManager;
        }

        public SpoofaxSyntaxHighlighter create(Lexer lexer) {
            return new SpoofaxSyntaxHighlighter(lexer, this.scopeManager);
        }
    }


    /**
     * Initializes a new instance of the {@link SpoofaxSyntaxHighlighter} class.
     *
     * @param lexer        The lexer instance to use for highlighting.
     * @param scopeManager The scope manager.
     */
    private SpoofaxSyntaxHighlighter(Lexer lexer, ScopeManager scopeManager) {
        this.lexer = lexer;
        this.scopeManager = scopeManager;
    }


    @Override public Lexer getHighlightingLexer() {
        return this.lexer;
    }

    @Override public TextAttributesKey[] getTokenHighlights(@Nullable IElementType tokenType) {
        if(!(tokenType instanceof SpoofaxTokenType))
            return this.scopeManager.EMPTY_KEYS;
        SpoofaxTokenType spoofaxTokenType = (SpoofaxTokenType) tokenType;
        return this.scopeManager.getTokenHighlights(spoofaxTokenType.getScope());
    }
}
