package mb.pipe.run.spoofax.sdf;

import java.util.ArrayList;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ITokenizer;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;

import mb.pipe.run.core.model.parse.TokenConstants;
import mb.pipe.run.core.model.parse.TokenImpl;
import mb.pipe.run.core.model.parse.TokenType;
import mb.pipe.run.core.model.region.Region;

public class TokenExtractor {
    public static ArrayList<mb.pipe.run.core.model.parse.Token> extract(IStrategoTerm ast) {
        final ImploderAttachment rootImploderAttachment = ImploderAttachment.get(ast);
        final ITokenizer tokenizer = rootImploderAttachment.getLeftToken().getTokenizer();
        final int tokenCount = tokenizer.getTokenCount();
        final ArrayList<mb.pipe.run.core.model.parse.Token> tokenStream = new ArrayList<>(tokenCount);
        int offset = -1;
        for(int i = 0; i < tokenCount; ++i) {
            final IToken jsglrToken = tokenizer.getTokenAt(i);
            if(tokenizer.isAmbigous() && jsglrToken.getStartOffset() < offset) {
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
            final mb.pipe.run.core.model.parse.Token token = convertToken(jsglrToken);
            tokenStream.add(token);
        }
        return tokenStream;
    }

    private static mb.pipe.run.core.model.parse.Token convertToken(IToken token) {
        final Region region = RegionFactory.fromToken(token);
        final TokenType tokenType = convertTokenKind(token.getKind());
        return new TokenImpl(region, tokenType, (IStrategoTerm) token.getAstNode());
    }

    private static TokenType convertTokenKind(int kind) {
        switch(kind) {
            case IToken.TK_IDENTIFIER:
                return TokenConstants.identifierType;
            case IToken.TK_STRING:
                return TokenConstants.stringType;
            case IToken.TK_NUMBER:
                return TokenConstants.numberType;
            case IToken.TK_KEYWORD:
                return TokenConstants.keywordType;
            case IToken.TK_OPERATOR:
                return TokenConstants.operatorType;
            case IToken.TK_LAYOUT:
                return TokenConstants.layoutType;
            default:
                return TokenConstants.unknownType;
        }
    }
}
