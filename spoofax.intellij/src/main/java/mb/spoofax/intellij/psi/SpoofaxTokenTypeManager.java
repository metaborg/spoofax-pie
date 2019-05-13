package mb.spoofax.intellij.psi;

import com.intellij.lang.Language;
import com.intellij.psi.tree.TokenSet;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.HashMap;


public class SpoofaxTokenTypeManager {
    private final HashMap<String, SpoofaxTokenType> scopedElementTypes = new HashMap<>();

    private final Language language;

    @Inject public SpoofaxTokenTypeManager(Language language) {
        this.language = language;
    }

    public String getDefaultScope() { return "text"; }

    public TokenSet getWhitespaceTokens() {
        return TokenSet.create(
            getTokenType("text.whitespace")
        );
    }

    public TokenSet getCommentTokens() {
        return TokenSet.create(
            getTokenType("comment.block"),
            getTokenType("comment.line"),
            getTokenType("comment")
        );
    }

    public TokenSet getStringLiteralTokens() {
        return TokenSet.create(
            getTokenType("string.quoted.single"),
            getTokenType("string.quoted.double"),
            getTokenType("string.quoted.triple"),
            getTokenType("string.quoted"),
            getTokenType("string.unquoted"),
            getTokenType("string.interpolated"),
            getTokenType("string.regexp"),
            getTokenType("string")
        );
    }

    public SpoofaxTokenType getTokenType(@Nullable String scope) {
        scope = scope != null ? scope : getDefaultScope();
        return this.scopedElementTypes.computeIfAbsent(scope, s -> new SpoofaxTokenType(s, this.language));
    }
}
