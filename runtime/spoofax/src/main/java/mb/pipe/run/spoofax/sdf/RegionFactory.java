package mb.pipe.run.spoofax.sdf;

import org.spoofax.jsglr.client.imploder.IToken;

import mb.pipe.run.core.model.region.IRegion;
import mb.pipe.run.core.model.region.Region;

public class RegionFactory {
    public static IRegion fromToken(IToken token) {
        return new Region(token.getStartOffset(), token.getEndOffset());
    }

    public static IRegion fromTokens(IToken left, IToken right) {
        return new Region(left.getStartOffset(), right.getEndOffset());
    }

    public static IRegion fromTokensLayout(IToken left, IToken right, boolean isNullable) {
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
