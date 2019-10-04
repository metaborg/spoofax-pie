package mb.common.region;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Objects;

/**
 * A region represents a finite region in source code text.
 *
 * A region has a start and end offset, represented by the
 * number of characters from the beginning of the source
 * text, with interval [0,#chars).
 */
public class Region implements Serializable {
    private final int startOffset;
    private final int endOffset;

    /**
     * Initializes a new instance of the {@link Region} class.
     *
     * @param startOffset The zero-based start offset of the first character in the region.
     * @param endOffset The zero-based end offset of the next character following the region.
     */
    private Region(int startOffset, int endOffset) {
        if(startOffset < 0)
            throw new IllegalArgumentException("The start offset " + startOffset + " must be positive or zero");
        if(endOffset < startOffset)
            throw new IllegalArgumentException("The end offset " + endOffset + " must be at or after the start offset " + startOffset);

        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    /**
     * Creates a new {@link Region} from the specified start and end offsets.
     *
     * @param startOffset The zero-based start offset of the first character in the region.
     * @param endOffset The zero-based end offset of the next character following the region.
     * @return The region.
     */
    public static Region fromOffsets(int startOffset, int endOffset) {
        return new Region(startOffset, endOffset);
    }

    /**
     * Creates a new {@link Region} from the specified start offset and length.
     *
     * @param startOffset The zero-based start offset of the first character in the region.
     * @param length The number of characters in the region.
     * @return The region.
     */
    public static Region fromOffsetLength(int startOffset, int length) {
        if (length < 0)
            throw new IllegalArgumentException("The length " + length + " must be positive or zero");

        return new Region(startOffset, startOffset + length);
    }

    /**
     * Parses a new {@link Region} from a string.
     * @param regionStr The string in the format "m-n" or "n", where m and n are integers.
     * @return The region.
     * @throws IllegalArgumentException The string could not be parsed.
     */
    public static Region fromString(String regionStr) throws IllegalArgumentException {
        final int dashIndex = regionStr.indexOf('-');
        if(dashIndex < 0) {
            final int offset = Integer.parseInt(regionStr);
            return Region.fromOffsets(offset, offset);
        } else {
            final String start = regionStr.substring(0, dashIndex);
            final int startOffset = Integer.parseInt(start);
            final String end = regionStr.substring(dashIndex);
            final int endOffset = Integer.parseInt(end);
            return Region.fromOffsets(startOffset, endOffset);
        }
    }


    /**
     * Gets the inclusive start offset.
     *
     * @return Zero-based inclusive starting offset, with interval [0,#chars).
     */
    public int getStartOffset() {
        return startOffset;
    }

    /**
     * Gets the exclusive end offset.
     *
     * @return Zero-based exclusive ending offset, with interval [0,#chars].
     */
    public int getEndOffset() {
        return endOffset;
    }

    /**
     * Gets the inclusive end offset.
     *
     * @return Zero-based inclusive ending offset with interval [0,#chars).
     * @deprecated Use {#getEndOffset()}, which is exclusive, instead.
     */
    @Deprecated public int getEndOffsetInclusive() {
        return endOffset - 1;
    }


    /**
     * Gets the length of the region.
     *
     * @return The number of characters in the region.
     */
    public int length() {
        return getEndOffset() - getStartOffset();
    }

    /**
     * Gets whether the region is empty.
     *
     * @return {@code true} when the region is empty; otherwise, {@code false}.
     */
    public boolean isEmpty() { return length() == 0; }

    /**
     * Checks if this region completely contains the given region.
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
        if (this.startOffset == this.endOffset)
            return Integer.toString(this.startOffset);
        else
            return startOffset + "-" + endOffset;
    }
}
