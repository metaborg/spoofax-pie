package mb.common.region;

import java.io.Serializable;
import java.util.Objects;

/**
 * A region represents a finite region in source code text. A region has a start and end offset, represented by
 * the number of characters from the beginning of the source text, with interval [0,#chars). Both the starting and
 * ending numbers are inclusive.
 */
public class Region implements Serializable {
    /**
     * Inclusive starting offset, the number of characters from the beginning of the source text with interval [0,#chars).
     */
    // DP: Better as a getter
    public final int startOffset;

    /**
     * Inclusive ending offset, the number of characters from the beginning of the source text with interval [0,#chars).
     */
    // DP: Why is it inclusive? This makes math very confusing. An empty file would need to have Region(0, -1).
    public final int endOffset;


    /**
     * @param startOffset The zero-based inclusive start offset.
     * @param endOffset The zero-based inclusive end offset.
     */
    public Region(int startOffset, int endOffset) {
        if (startOffset < 0)
            throw new IllegalArgumentException("The start offset " +  startOffset + " must be positive or zero.");
        if (endOffset < (startOffset - 1))
            throw new IllegalArgumentException("The end offset " + endOffset + " must be after the start offset " + startOffset + ".");

        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    /**
     * @return The length of the region.
     */
    public int length() {
        return (this.endOffset - this.startOffset) + 1;
    }

    /**
     * Checks if this region contains given region.
     *
     * @param region Other region to check.
     * @return {@code true} if this region contains given region, {@code false} otherwise.
     */
    public boolean contains(Region region) {
        return region.startOffset >= this.startOffset &&
            region.startOffset <= this.endOffset &&
            region.endOffset <= this.endOffset;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final Region region = (Region) o;
        return startOffset == region.startOffset && endOffset == region.endOffset;
    }

    @Override public int hashCode() {
        return Objects.hash(startOffset, endOffset);
    }

    @Override public String toString() {
        return startOffset + "-" + endOffset;
    }
}
