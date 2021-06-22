package mb.jsglr.common;

import mb.common.region.Region;
import mb.common.token.Token;
import mb.common.token.TokenImpl;
import mb.common.token.TokenType;
import mb.common.token.TokenTypes;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ITokens;

import java.util.ArrayList;

public class TokenUtil {
    public static JSGLRTokens extract(ITokens tokens, boolean ambiguous) {
        final ArrayList<Token<IStrategoTerm>> tokenStream = new ArrayList<>(tokens.getTokenCount());
        int offset = -1;
        for(IToken jsglrToken : tokens) {
            if(ambiguous && jsglrToken.getStartOffset() < offset) {
                // In case of ambiguities, tokens inside the ambiguity are duplicated, ignore.
                continue;
            }
            if(jsglrToken.getStartOffset() > jsglrToken.getEndOffset()) {
                // Indicates an invalid region. Empty lists have regions like this.
                continue;
            }
            if(offset >= jsglrToken.getStartOffset()) {
                // Duplicate region, skip.
                continue;
            }
            offset = jsglrToken.getEndOffset();
            final Token<IStrategoTerm> token = convertToken(jsglrToken);
            tokenStream.add(token);
        }
        return new JSGLRTokens(tokenStream);
    }

    public static Token<IStrategoTerm> convertToken(IToken token) {
        final TokenType tokenType = convertTokenKind(token.getKind());
        final Region region = RegionUtil.fromToken(token);
        final IStrategoTerm fragment = (IStrategoTerm)token.getAstNode();
        return new TokenImpl<>(tokenType, region, fragment);
    }

    public static TokenType convertTokenKind(IToken.Kind kind) {
        switch(kind) {
            case TK_IDENTIFIER:
                return TokenTypes.identifier();
            case TK_STRING:
                return TokenTypes.string();
            case TK_NUMBER:
                return TokenTypes.number();
            case TK_KEYWORD:
                return TokenTypes.keyword();
            case TK_OPERATOR:
                return TokenTypes.operator();
            case TK_LAYOUT:
                return TokenTypes.layout();
            default:
                return TokenTypes.unknown();
        }
    }
}
