package mb.common.region;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Objects;

/**
 * A region represents a finite region in source code text.
 * A region has a start and end offset, represented by the
 * number of characters from the beginning of the source text, with interval [0,#chars).
 */
public final class Region implements Serializable {

    private final int startOffset;
    private final int endOffset;

    private Region(int startOffset, int endOffset) {
        if(startOffset < 0)
            throw new IllegalArgumentException("The start offset " + startOffset + " must be positive or zero.");
        if(endOffset < startOffset)
            throw new IllegalArgumentException("The end offset " + endOffset + " must be greater than or equal to the start offset " + startOffset + ".");

        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    /**
     * Creates a new region with the specified start and end offsets.
     *
     * @param startOffset the zero-based offset of the start of the region
     * @param endOffset the zero-based offset of the end of the region, exclusive
     * @return the created region
     */
    public static Region fromOffsets(int startOffset, int endOffset) {
        return new Region(startOffset, endOffset);
    }

    /**
     * Creates a new region with the specified start and end offsets.
     *
     * @param startOffset the zero-based offset of the start of the region
     * @param length the length of the region
     * @return the created region
     */
    public static Region fromOffsetLength(int startOffset, int length) {
        return new Region(startOffset, startOffset + length);
    }

    /**
     * Attempts to create a new region by parsing the specified region string.
     *
     * @param regionStr the region string, in the format "start-end" or "offset"
     * @return the parsed region; or {@code null} when parsing failed
     */
    public static @Nullable Region fromString(String regionStr) {
        try {
            final int dashIndex = regionStr.indexOf('-');
            if(dashIndex < 0) {
                final int offset = Integer.parseInt(regionStr);
                return Region.fromOffsets(offset, offset);
            } else {
                final String start = regionStr.substring(0, dashIndex);
                final int startOffset = Integer.parseInt(start);
                final String end = regionStr.substring(dashIndex + 1);
                final int endOffset = Integer.parseInt(end);
                return Region.fromOffsets(startOffset, endOffset);
            }
        } catch (IllegalArgumentException e) {
            return null;
        }
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
     * Gets the number of characters in the region.
     *
     * @return the length of the region; which may be zero
     */
    public int getLength() {
        return getEndOffset() - getStartOffset();
    }

    /**
     * Gets whether the region is empty.
     *
     * @return {@code true} when the region is empty; otherwise, {@code false}
     */
    public boolean isEmpty() { return getLength() == 0; }

    /**
     * Checks if this region contains given region.
     * <p>
     * Invariant: if (a contains b) and (b contains a) then (a == b).
     * Invariant: if (a contains b) or (b contains a) then (a intersects b).
     *
     * @param region the other region to check
     * @return {@code true} if this region contains the given region; otherwise, {@code false}
     */
    public boolean contains(Region region) {
        // @formatter:off
        return region.getStartOffset() >= this.getStartOffset()
            && region.getStartOffset() <= this.getEndOffset()
            && region.getEndOffset()   <= this.getEndOffset();
        // @formatter:on
    }

    /**
     * Checks if this region intersects the given region.
     *
     * Invariant: if this method returns {@code true},
     * then {@link Region#intersectionOf(Region, Region)} )} will not return {@code null}.
     *
     * @param region the other region to check
     * @return {@code true} if this region intersects the given region; otherwise, {@code false}
     */
    public boolean intersectsWith(Region region) {
        // @formatter:off
        return region.getStartOffset() <= this.getEndOffset()
            && region.getEndOffset()   >= this.getStartOffset();
        // @formatter:on
    }

    /**
     * Gets the intersection of this region with the specified region.
     *
     * Invariant: a contains (a intersectionWith b)
     * Invariant: b contains (a intersectionWith b)
     * Invariant: if (a intersectionWith b) == a then (b contains a).
     * Invariant: if (a intersectionWith b) == b then (a contains b).
     *
     * @param a one region to find the intersection with
     * @param b another region to find the intersection with
     * @return the intersection of both regions, which may be empty; or {@code null} when there is no intersection
     */
    public static @Nullable Region intersectionOf(Region a, Region b) {
        if (!a.intersectsWith(b)) return null;
        return new Region(
            Math.max(a.startOffset, b.startOffset),
            Math.min(a.endOffset, b.endOffset)
        );
    }

    @Override public boolean equals(@Nullable Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;
        final Region other = (Region) obj;
        // @formatter:off
        return startOffset == other.startOffset
            && endOffset == other.endOffset;
        // @formatter:on
    }

    @Override public int hashCode() {
        return Objects.hash(startOffset, endOffset);
    }

    @Override public String toString() {
        return isEmpty() ? "" + startOffset : startOffset + "-" + endOffset;
    }
}
