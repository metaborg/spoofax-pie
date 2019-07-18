package mb.common.region;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Objects;

/**
 * A region represents a finite region in source code text. A region has a start and end offset, represented by the
 * number of characters from the beginning of the source text, with interval [0,#chars).
 */
public class Region implements Serializable {
    private final int startOffsetInclusive;
    private final int endOffsetExclusive;


    /**
     * @param startOffsetInclusive The zero-based inclusive start offset.
     * @param endOffsetInclusive   The zero-based inclusive end offset.
     */
    public Region(int startOffsetInclusive, int endOffsetInclusive) {
        if(startOffsetInclusive < 0)
            throw new IllegalArgumentException("The start offset " + startOffsetInclusive + " must be positive or zero.");
        if(endOffsetInclusive < (startOffsetInclusive - 1))
            throw new IllegalArgumentException("The end offset " + endOffsetInclusive + " must be after the start offset " + startOffsetInclusive + ".");

        this.startOffsetInclusive = startOffsetInclusive;
        this.endOffsetExclusive = endOffsetInclusive + 1;
    }


    /**
     * @return Inclusive starting offset with interval [0,#chars).
     */
    public int getStartOffset() {
        return startOffsetInclusive;
    }

    /**
     * @return Exclusive ending offset with interval [0,#chars].
     */
    public int getEndOffset() {
        return endOffsetExclusive;
    }

    /**
     * @return Inclusive ending offset with interval [0,#chars).
     * @deprecated Use {#getEndOffset()}, which is exclusive, instead.
     */
    @Deprecated public int getEndOffsetInclusive() {
        return endOffsetExclusive - 1;
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
        return startOffsetInclusive == other.startOffsetInclusive &&
            endOffsetExclusive == other.endOffsetExclusive;
    }

    @Override public int hashCode() {
        return Objects.hash(startOffsetInclusive, endOffsetExclusive);
    }

    @Override public String toString() {
        return startOffsetInclusive + "-" + endOffsetExclusive;
    }
}
