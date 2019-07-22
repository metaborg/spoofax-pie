package mb.common.region;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Objects;

/**
 * A region represents a finite region in source code text. A region has a start and end offset, represented by the
 * number of characters from the beginning of the source text, with interval [0,#chars).
 */
public class Region implements Serializable {
    private final int startOffset;
    private final int endOffset;


    private Region(int startOffset, int endOffset) {
        if(startOffset < 0)
            throw new IllegalArgumentException("The start offset " + startOffset + " must be positive or zero");
        if(endOffset < startOffset)
            throw new IllegalArgumentException("The end offset " + endOffset + " must be after the start offset " + startOffset);

        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    public static Region fromOffsets(int startOffset, int endOffset) {
        return new Region(startOffset, endOffset);
    }

    public static Region fromOffsetLength(int startOffset, int length) {
        return new Region(startOffset, startOffset + length);
    }


    /**
     * @return Inclusive starting offset with interval [0,#chars).
     */
    public int getStartOffset() {
        return startOffset;
    }

    /**
     * @return Exclusive ending offset with interval [0,#chars].
     */
    public int getEndOffset() {
        return endOffset;
    }

    /**
     * @return Inclusive ending offset with interval [0,#chars).
     * @deprecated Use {#getEndOffset()}, which is exclusive, instead.
     */
    @Deprecated public int getEndOffsetInclusive() {
        return endOffset - 1;
    }


    /**
     * @return The length of the region.
     */
    public int length() {
        return getEndOffset() - getStartOffset();
    }

    /**
     * Checks if this region contains given region.
     *
     * @param region Other region to check.
     * @return {@code true} if this region contains given region, {@code false} otherwise.
     */
    public boolean contains(Region region) {
        return region.getStartOffset() >= this.getStartOffset() &&
            region.getStartOffset() <= this.getEndOffset() &&
            region.getEndOffset() <= this.getEndOffset();
    }


    @Override public boolean equals(@Nullable Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;
        final Region other = (Region) obj;
        return startOffset == other.startOffset &&
            endOffset == other.endOffset;
    }

    @Override public int hashCode() {
        return Objects.hash(startOffset, endOffset);
    }

    @Override public String toString() {
        return startOffset + "-" + endOffset;
    }
}
