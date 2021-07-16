package mb.jsglr.common;

import mb.common.region.Region;
import org.spoofax.jsglr.client.imploder.IToken;

public class RegionUtil {
    public static Region fromToken(IToken token) {
        return fromTokens(token, token);
    }

    public static Region fromTokens(IToken left, IToken right) {
        final int startOffset = Math.max(left.getStartOffset(), 0);
        final int endOffset = Math.max(right.getEndOffset() + 1, startOffset);
        final int startLine = Math.max(left.getLine(), 0);
        final int endLine = Math.max(right.getEndLine(), startLine);
        return Region.fromOffsets(startOffset, endOffset, startLine, endLine);
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

        return Region.fromOffsets(leftStartOffset, rightEndOffset + 1, left.getLine(), right.getEndLine());
    }
}
