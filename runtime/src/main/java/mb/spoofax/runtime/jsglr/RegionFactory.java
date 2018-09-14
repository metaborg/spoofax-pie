package mb.spoofax.runtime.jsglr;

import mb.spoofax.api.region.Region;
import org.spoofax.jsglr.client.imploder.IToken;

public class RegionFactory {
    public static Region fromToken(IToken token) {
        return new Region(token.getStartOffset(), token.getEndOffset());
    }

    public static Region fromTokens(IToken left, IToken right) {
        return new Region(left.getStartOffset(), right.getEndOffset());
    }

    public static Region fromTokensLayout(IToken left, IToken right, boolean isNullable) {
        int leftStartOffset = left.getStartOffset();
        int rightEndOffset = right.getEndOffset();

        // To fix the difference between offset and cursor position
        if(left.getKind() != IToken.TK_LAYOUT && !isNullable) {
            leftStartOffset++;
        }

        if(isNullable) {
            rightEndOffset++;
        }

        return new Region(leftStartOffset, rightEndOffset);
    }
}
