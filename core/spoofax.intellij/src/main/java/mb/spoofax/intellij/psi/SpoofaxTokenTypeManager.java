package mb.spoofax.intellij.psi;

import com.intellij.psi.tree.TokenSet;
import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.intellij.IntellijLanguage;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.HashMap;

/**
 * Manages token types for Spoofax languages.
 *
 * IntelliJ used TokenTypes to distinguish and style different kinds of tokens.
 * Since Spoofax lacks this concept, we dynamically create a unique token types
 * to represent each unique style (scope) of token, to allow them to be distinguished by IntelliJ.
 */
@LanguageScope
public final class SpoofaxTokenTypeManager {
    /** Caches the created token types. */
    private final HashMap<String, SpoofaxTokenType> scopedElementTypes = new HashMap<>();

    /** The language for which token types are created. */
    private final IntellijLanguage language;


    /**
     * Initializes a new instance of the {@link SpoofaxTokenTypeManager} class.
     *
     * @param language The language for which the token types are created.
     */
    @Inject public SpoofaxTokenTypeManager(IntellijLanguage language) {
        this.language = language;
    }


    /**
     * Gets the default scope name.
     *
     * @return The default scope name.
     */
    public String getDefaultScope() { return "text"; }

    /**
     * Gets the token types for whitespace tokens.
     *
     * @return A set of token types.
     */
    public TokenSet getWhitespaceTokens() {
        return TokenSet.create(
            getTokenType("text.whitespace")
        );
    }

    /**
     * Gets the token types for comment tokens.
     *
     * @return A set of token types.
     */
    public TokenSet getCommentTokens() {
        return TokenSet.create(
            getTokenType("comment.block"),
            getTokenType("comment.line"),
            getTokenType("comment")
        );
    }

    /**
     * Gets the token types for string literal tokens.
     *
     * @return A set of token types.
     */
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

    /**
     * Gets (or creates) the token type for the specified scope.
     *
     * @param simplifiedScopeName The simplfiied scope name.
     * @return The token type.
     */
    public SpoofaxTokenType getTokenType(@Nullable String simplifiedScopeName) {
        simplifiedScopeName = simplifiedScopeName != null ? simplifiedScopeName : getDefaultScope();
        return this.scopedElementTypes.computeIfAbsent(simplifiedScopeName, s -> new SpoofaxTokenType(s, this.language));
    }
}
