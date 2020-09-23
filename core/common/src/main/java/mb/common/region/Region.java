package mb.common.region;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Objects;
import java.util.OptionalInt;

/**
 * A region represents a finite region in source code text. A region has a start and end offset, represented by the
 * number of characters from the beginning of the source text, with interval [0,#chars). Additionally, a region may
 * optionally contain a start and end line number, with interval [0,#lines).
 */
public final class Region implements Serializable {
    private final int startOffset;
    private final int endOffset;
    private final int startLine;
    private final int endLine;

    private Region(int startOffset, int endOffset, int startLine, int endLine) {
        if(startOffset < 0)
            throw new IllegalArgumentException("The start offset " + startOffset + " must be positive or zero");
        if(endOffset < startOffset)
            throw new IllegalArgumentException("The end offset " + endOffset + " must be greater than or equal to the start offset " + startOffset);
        if(startLine < -1)
            throw new IllegalArgumentException("The start line " + startLine + " must be positive, zero, or -1 indicating that there is no line information");
        if(endLine < startLine)
            throw new IllegalArgumentException("The end line " + endLine + " must be greater than or equal to the start line " + startLine);

        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.startLine = startLine;
        this.endLine = endLine;
    }

    private Region(int startOffset, int endOffset) {
        this(startOffset, endOffset, -1, -1);
    }

    /**
     * Creates a new region at the offset.
     *
     * @param offset the zero-based offset of the start and end of the region
     * @return the created region
     */
    public static Region atOffset(int offset) {
        return fromOffsets(offset, offset);
    }

    /**
     * Creates a new region with the specified start and end offsets.
     *
     * @param startOffset the zero-based offset of the start of the region
     * @param endOffset   the zero-based offset of the end of the region, exclusive
     * @return the created region
     */
    public static Region fromOffsets(int startOffset, int endOffset) {
        return new Region(startOffset, endOffset);
    }

    /**
     * Creates a new region with the specified start and end offsets, and a line number.
     *
     * @param startOffset the zero-based offset of the start of the region
     * @param endOffset   the zero-based offset of the end of the region, exclusive
     * @param line        the zero-based line number
     * @return the created region
     */
    public static Region fromOffsets(int startOffset, int endOffset, int line) {
        return new Region(startOffset, endOffset, line, line);
    }

    /**
     * Creates a new region with the specified start and end offsets, and a starting and ending line number.
     *
     * @param startOffset the zero-based offset of the start of the region
     * @param endOffset   the zero-based offset of the end of the region, exclusive
     * @param startLine   the zero-based line number at the start of the region
     * @param endLine     the zero-based line number at the end of the region, exclusive
     * @return the created region
     */
    public static Region fromOffsets(int startOffset, int endOffset, int startLine, int endLine) {
        return new Region(startOffset, endOffset, startLine, endLine);
    }

    /**
     * Creates a new region with the specified start and end offsets.
     *
     * @param startOffset the zero-based offset of the start of the region
     * @param length      the length of the region
     * @return the created region
     */
    public static Region fromOffsetLength(int startOffset, int length) {
        return fromOffsets(startOffset, startOffset + length);
    }

    /**
     * Attempts to create a new region by parsing the specified region string.
     *
     * @param str the string to parse, in the format "start-end" or "offset"
     * @return the parsed region; or {@code null} when parsing failed
     */
    public static @Nullable Region fromString(String str) {
        try {
            final int dashIndex = str.indexOf('-');
            if(dashIndex <= 0) {
                final int offset = Integer.parseInt(str);
                return Region.fromOffsets(offset, offset);
            } else {
                final String start = str.substring(0, dashIndex);
                final int startOffset = Integer.parseInt(start);
                final String end = str.substring(dashIndex + 1);
                final int endOffset = Integer.parseInt(end);
                return Region.fromOffsets(startOffset, endOffset);
            }
        } catch(IllegalArgumentException e) {
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
     * @return Inclusive starting line number with interval [0,#lines), or empty when the starting line is unknown.
     */
    public OptionalInt getStartLine() { return startLine >= 0 ? OptionalInt.of(startLine) : OptionalInt.empty(); }

    /**
     * @return Exclusive ending line number with interval [0,#lines], or empty when the ending line is unknown.
     */
    public OptionalInt getEndLine() { return endLine >= 0 ? OptionalInt.of(endLine) : OptionalInt.empty(); }


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
     * Invariant: if (a contains b) and (b contains a) then (a == b). Invariant: if (a contains b) or (b contains a)
     * then (a intersects b).
     * <p>
     * Note that calling this implementation with an empty region is different from calling the other {@link
     * #contains(int)} overload with an offset. In this implementation, an empty region is considered to be in a region
     * when (start <= offset <= end).
     * <p>
     * When this region is empty, it can only contain the other region when that is also empty.
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
     * Checks if this region contains the given offset.
     * <p>
     * Note that calling this implementation with an offset is different from calling the other {@link
     * #contains(Region)} overload with an empty region. In this implementation, an offset is considered to be in a
     * region when (start <= offset < end).
     * <p>
     * When this region is empty, it contains no offsets.
     *
     * @param offset the offset to check
     * @return {@code true} if this region contains the given offset; otherwise, {@code false}.
     */
    public boolean contains(int offset) {
        return this.getStartOffset() <= offset && offset < this.getEndOffset();
    }

    /**
     * Checks if this region intersects the given region.
     *
     * Invariant: if this method returns {@code true}, then {@link Region#intersectionOf(Region, Region)} )} will not
     * return {@code null}.
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
     * Invariant: a contains (a intersectionWith b) Invariant: b contains (a intersectionWith b) Invariant: if (a
     * intersectionWith b) == a then (b contains a). Invariant: if (a intersectionWith b) == b then (a contains b).
     *
     * @param a one region to find the intersection with
     * @param b another region to find the intersection with
     * @return the intersection of both regions, which may be empty; or {@code null} when there is no intersection
     */
    public static @Nullable Region intersectionOf(Region a, Region b) {
        if(!a.intersectsWith(b)) return null;
        return new Region(
            Math.max(a.startOffset, b.startOffset),
            Math.min(a.endOffset, b.endOffset)
        );
    }


    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final Region region = (Region)o;
        return startOffset == region.startOffset &&
            endOffset == region.endOffset &&
            startLine == region.startLine &&
            endLine == region.endLine;
    }

    @Override public int hashCode() {
        return Objects.hash(startOffset, endOffset, startLine, endLine);
    }

    @Override public String toString() {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(startOffset);
        if(!isEmpty()) {
            stringBuilder.append("-").append(endOffset);
        }
        if(startLine != -1) {
            stringBuilder.append("@").append(startLine);
        }
        if(endLine != -1 && startLine != endLine) {
            stringBuilder.append("-").append(endLine);
        }
        return stringBuilder.toString();
    }
}
