package mb.spoofax.runtime.sdf;

import org.spoofax.jsglr.client.imploder.IToken;

import mb.spoofax.api.region.Region;
import mb.spoofax.api.region.RegionImpl;

public class RegionFactory {
    public static Region fromToken(IToken token) {
        return new RegionImpl(token.getStartOffset(), token.getEndOffset());
    }

    public static Region fromTokens(IToken left, IToken right) {
        return new RegionImpl(left.getStartOffset(), right.getEndOffset());
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

        return new RegionImpl(leftStartOffset, rightEndOffset);
    }
}
