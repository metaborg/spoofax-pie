package mb.jsglr1.common;

import mb.common.region.Region;
import org.spoofax.jsglr.client.imploder.IToken;

class RegionUtil {
    static Region fromToken(IToken token) {
        return new Region(token.getStartOffset(), token.getEndOffset());
    }

    static Region fromTokens(IToken left, IToken right) {
        return new Region(left.getStartOffset(), right.getEndOffset());
    }

    static Region fromTokensLayout(IToken left, IToken right, boolean isNullable) {
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
