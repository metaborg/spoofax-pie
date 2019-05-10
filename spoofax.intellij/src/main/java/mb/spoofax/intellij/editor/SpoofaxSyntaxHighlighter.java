package mb.spoofax.intellij.editor;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;

import javax.annotation.Nullable;
import javax.inject.Inject;

public final class SpoofaxSyntaxHighlighter extends SyntaxHighlighterBase {
    private final Lexer lexer;
    private final ScopeManager scopeManager;


    public static class Factory {
        private final ScopeManager scopeManager;

        @Inject public Factory(ScopeManager scopeManager) {
            this.scopeManager = scopeManager;
        }

        public SpoofaxSyntaxHighlighter create(Lexer lexer) {
            return new SpoofaxSyntaxHighlighter(lexer, scopeManager);
        }
    }

    public SpoofaxSyntaxHighlighter(Lexer lexer, ScopeManager scopeManager) {
        this.lexer = lexer;
        this.scopeManager = scopeManager;
    }


    @Override public Lexer getHighlightingLexer() {
        return this.lexer;
    }

    @Override public TextAttributesKey[] getTokenHighlights(@Nullable IElementType tokenType) {
        // TODO: fix code
//        if(!(tokenType instanceof SpoofaxTokenType))
//            return this.scopeManager.EMPTY_KEYS;
//        SpoofaxTokenType spoofaxTokenType = tokenType;
//        return this.scopeManager.getTokenHighlights(spoofaxTokenType.scope);
        return this.scopeManager.EMPTY_KEYS;
    }
}
