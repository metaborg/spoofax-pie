package mb.jsglr.common;

import mb.common.region.Region;
import org.spoofax.jsglr.client.imploder.IToken;

public class RegionUtil {
    public static Region fromToken(IToken token) {
        return Region.fromOffsets(token.getStartOffset(), token.getEndOffset() + 1);
    }

    public static Region fromTokens(IToken left, IToken right) {
        return Region.fromOffsets(left.getStartOffset(), right.getEndOffset() + 1);
    }

    public static Region fromTokensLayout(IToken left, IToken right, boolean isNullable) {
        int leftStartOffset = left.getStartOffset();
        int rightEndOffset = right.getEndOffset();

        // To fix the difference between offset and cursor position
        if(left.getKind() != IToken.Kind.TK_LAYOUT && !isNullable) {
            leftStartOffset++;
        }

        if(isNullable) {
            rightEndOffset++;
        }

        return Region.fromOffsets(leftStartOffset, rightEndOffset + 1);
    }
}
