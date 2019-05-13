package mb.spoofax.intellij.editor;

import com.google.common.collect.Lists;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import javafx.util.Pair;
import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.intellij.ScopeNames;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@LanguageScope
public final class ScopeManager {

    @Inject
    public ScopeManager() {}

    public final TextAttributesKey[] EMPTY_KEYS = new TextAttributesKey[0];
    public final String DEFAULT_SCOPE = "text";
    private final TextAttributesKey BAD_CHARACTER = TextAttributesKey.createTextAttributesKey("AESI_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER);
    private final List<Pair<String, TextAttributesKey[]>> styleScopes = Lists.newArrayList(
        // Put more specific scopes (longer prefixes) before more general scopes (shorter prefixes).
        // See https://manual.macromates.com/en/language_grammars for details about these scopes.
        createScopeStyle("text.whitespace", HighlighterColors.TEXT),     // TODO: Whitespace
        createScopeStyle("text", HighlighterColors.TEXT),
        createScopeStyle("source", HighlighterColors.TEXT),
        // Comments
        createScopeStyle("comment.line", DefaultLanguageHighlighterColors.LINE_COMMENT),
        createScopeStyle("comment.block", DefaultLanguageHighlighterColors.BLOCK_COMMENT),
        createScopeStyle("comment.block.documentation", DefaultLanguageHighlighterColors.DOC_COMMENT),
        createScopeStyle("comment", DefaultLanguageHighlighterColors.BLOCK_COMMENT),
        // Constants
        createScopeStyle("constant.numeric", DefaultLanguageHighlighterColors.NUMBER),
        createScopeStyle("constant.character.escape", DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE),
        createScopeStyle("constant.character", DefaultLanguageHighlighterColors.STRING),
        createScopeStyle("constant", DefaultLanguageHighlighterColors.CONSTANT),
        // Entities
        createScopeStyle("entity.name.function", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION),
        createScopeStyle("entity.name.type", DefaultLanguageHighlighterColors.CLASS_NAME),
        createScopeStyle("entity.name", DefaultLanguageHighlighterColors.CLASS_NAME),
        createScopeStyle("entity.other.inherited-class", DefaultLanguageHighlighterColors.CLASS_NAME),
        createScopeStyle("entity.other.attribute-name", DefaultLanguageHighlighterColors.CLASS_NAME),
        createScopeStyle("entity.other", DefaultLanguageHighlighterColors.CLASS_NAME),
        // Invalid
        createScopeStyle("invalid.illegal", BAD_CHARACTER),
        createScopeStyle("invalid.deprecated", HighlighterColors.TEXT),    // TODO: Strikethrough text
        createScopeStyle("invalid", HighlighterColors.TEXT),    // TODO: Red text
        // Keywords
        createScopeStyle("keyword.operator", DefaultLanguageHighlighterColors.OPERATION_SIGN),
        createScopeStyle("keyword.control", DefaultLanguageHighlighterColors.KEYWORD),
        createScopeStyle("keyword", DefaultLanguageHighlighterColors.KEYWORD),
        // Markup
        createScopeStyle("markup.underline.link", DefaultLanguageHighlighterColors.KEYWORD),
        createScopeStyle("markup.underline", DefaultLanguageHighlighterColors.KEYWORD),
        createScopeStyle("markup.bold", DefaultLanguageHighlighterColors.KEYWORD),
        createScopeStyle("markup.italic", DefaultLanguageHighlighterColors.KEYWORD),
        createScopeStyle("markup.list", DefaultLanguageHighlighterColors.KEYWORD),
        createScopeStyle("markup.quote", DefaultLanguageHighlighterColors.KEYWORD),
        createScopeStyle("markup.raw", DefaultLanguageHighlighterColors.KEYWORD),
        // Meta
        createScopeStyle("meta.braces", DefaultLanguageHighlighterColors.BRACES),
        createScopeStyle("meta.parens", DefaultLanguageHighlighterColors.PARENTHESES),
        createScopeStyle("meta.brackets", DefaultLanguageHighlighterColors.BRACKETS),
        createScopeStyle("meta.generic", DefaultLanguageHighlighterColors.BRACKETS),
        // Punctuation
        createScopeStyle("punctuation.separator", DefaultLanguageHighlighterColors.COMMA),
        createScopeStyle("punctuation.terminator", DefaultLanguageHighlighterColors.SEMICOLON),
        createScopeStyle("punctuation.accessor", DefaultLanguageHighlighterColors.DOT),
        // Storage
        createScopeStyle("storage.type", DefaultLanguageHighlighterColors.KEYWORD),
        createScopeStyle("storage.modifier", DefaultLanguageHighlighterColors.KEYWORD),
        createScopeStyle("storage", DefaultLanguageHighlighterColors.KEYWORD),
        // Strings
        createScopeStyle("string.quoted.single", DefaultLanguageHighlighterColors.STRING),
        createScopeStyle("string.quoted.double", DefaultLanguageHighlighterColors.STRING),
        createScopeStyle("string.quoted.triple", DefaultLanguageHighlighterColors.STRING),
        createScopeStyle("string.quoted", DefaultLanguageHighlighterColors.STRING),
        createScopeStyle("string.unquoted", DefaultLanguageHighlighterColors.STRING),
        createScopeStyle("string.interpolated", DefaultLanguageHighlighterColors.STRING),
        createScopeStyle("string.regexp", DefaultLanguageHighlighterColors.STRING),
        createScopeStyle("string", DefaultLanguageHighlighterColors.STRING),
        // Support
        createScopeStyle("support.function", DefaultLanguageHighlighterColors.PREDEFINED_SYMBOL),
        createScopeStyle("support.class", DefaultLanguageHighlighterColors.PREDEFINED_SYMBOL),
        createScopeStyle("support.type", DefaultLanguageHighlighterColors.PREDEFINED_SYMBOL),
        createScopeStyle("support.constant", DefaultLanguageHighlighterColors.PREDEFINED_SYMBOL),
        createScopeStyle("support.variable", DefaultLanguageHighlighterColors.PREDEFINED_SYMBOL),
        createScopeStyle("support", DefaultLanguageHighlighterColors.PREDEFINED_SYMBOL),
        // Variables
        createScopeStyle("variable.parameter", DefaultLanguageHighlighterColors.PARAMETER),
        createScopeStyle("variable.language", DefaultLanguageHighlighterColors.KEYWORD),
        createScopeStyle("variable", DefaultLanguageHighlighterColors.IDENTIFIER)
    );

    public String getSimplifiedScope(ScopeNames scopes) {
        return this.styleScopes.stream()
                .map(Pair::getKey)
                .filter(scopes::contains)
                .findFirst()
                .orElse(DEFAULT_SCOPE);
    }

    public TextAttributesKey[] getTokenHighlights(String scope) {
        return this.styleScopes.stream()
                .filter(p -> scope.startsWith(p.getKey()))
                .map(Pair::getValue)
                .findFirst()
                .orElse(EMPTY_KEYS);
    }

    private Pair<String, TextAttributesKey[]> createScopeStyle(String prefix, TextAttributesKey style) {
        return new Pair(prefix, new TextAttributesKey[]{ TextAttributesKey.createTextAttributesKey(createScopeName(prefix), style) } );
    }

    private String createScopeName(String prefix) {
        return "SPOOFAX_" + prefix.toUpperCase().replace('.', '_');
    }
}