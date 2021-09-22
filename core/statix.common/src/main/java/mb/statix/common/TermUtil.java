package mb.statix.common;

import mb.common.region.Region;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.stratego.TermOrigin;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;

public final class TermUtil {
    private TermUtil() { /* Prevent instantiation. */ }

    /**
     * Gets the origin region of the specified term.
     *
     * @param term the term
     * @return the origin's region; or {@code null} when it could not be determined
     */
    public static @Nullable Region getRegion(ITerm term) {
        @Nullable ImploderAttachment imploderAttachment = term.getAttachments().get(ImploderAttachment.class);
        if (imploderAttachment == null) {
            // Term didn't have an imploder attachment, but maybe it has an origin term with an imploder attachment?
            @Nullable final TermOrigin termOrigin = term.getAttachments().get(TermOrigin.class);
            if(termOrigin == null) return null;
            imploderAttachment = termOrigin.getImploderAttachment();
        }
        if (imploderAttachment == null) return null;
        final IToken leftToken = imploderAttachment.getLeftToken();
        final IToken rightToken = imploderAttachment.getRightToken();
        int startOffset = leftToken.getStartOffset();
        int endOffset = rightToken.getEndOffset();
        return Region.fromOffsets(startOffset, endOffset);
    }
}
