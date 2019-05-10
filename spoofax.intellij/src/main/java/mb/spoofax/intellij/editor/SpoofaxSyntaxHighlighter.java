package mb.spoofax.intellij.editor;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.inject.Inject;


public final class SpoofaxSyntaxHighlighter extends SyntaxHighlighterBase {

    private final Lexer lexer;
    private final ScopeManager scopeManager;

    @Inject
    public SpoofaxSyntaxHighlighter(
            @Assisted Lexer lexer,
            ScopeManager scopeManager
    ) {
        this.lexer = lexer;
        this.scopeManager = scopeManager;
    }

    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
        return this.lexer;
    }

    @NotNull
    @Override
    public TextAttributesKey[] getTokenHighlights(@Nullable IElementType tokenType) {
        if (!(tokenType instanceof SpoofaxTokenType))
            return this.scopeManager.EMPTY_KEYS;
        SpoofaxTokenType spoofaxTokenType = tokenType;
        return this.scopeManager.getTokenHighlights(spoofaxTokenType.scope);
    }

}
