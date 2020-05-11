package mb.spoofax.intellij;

import mb.common.region.Region;

import java.io.Serializable;
import java.util.Objects;


/**
 * Selects a span of text.
 */
public final class Span implements Serializable {

    /**
     * Creates a new {@link Span} from a start offset and a length.
     *
     * @param startOffset The zero-based inclusive start offset.
     * @param length The length of the span.
     * @return The created {@link Span}.
     */
    public static Span fromLength(int startOffset, int length) {
        if (startOffset < 0)
            throw new IllegalArgumentException("The start offset $startOffset must be positive or zero.");
        if (length < 0)
            throw new IllegalArgumentException("The length $length must be positive or zero.");
        return new Span(startOffset, startOffset + length);
    }

    private final int startOffset;
    private final int endOffset;

    /**
     * Initializes a new instance of the {@link Span} class.
     *
     * @param startOffset The zero-based inclusive start offset.
     * @param endOffset The zero-based exclusive end offset.
     */
    public Span(int startOffset, int endOffset) {
        if (startOffset < 0)
            throw new IllegalArgumentException("The start offset $startOffset must be positive or zero.");
        if (endOffset < startOffset)
            throw new IllegalArgumentException("The end offset $endOffset must be at or after the start offset $startOffset.");
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    /**
     * Gets the zero-based inclusive start offset of the span.
     *
     * The start offset is the zero-based offset of the first character in the span.
     *
     * @return The start offset.
     */
    public int getStartOffset() {
        return this.startOffset;
    }

    /**
     * Gets the zero-based exclusive end offset of the span.
     *
     * The end offset is the zero-based offset of the first character immediately following the span.
     *
     * @return The end offset.
     */
    public int getEndOffset() {
        return this.endOffset;
    }

    /**
     * Gets the number of characters in the span.
     *
     * @return The length of the span.
     */
    public int getLength() {
        return this.endOffset - this.startOffset;
    }

    /**
     * Gets whether the span is empty.
     *
     * @return True when the span is empty; otherwise, false.
     */
    public boolean isEmpty() {
        return this.startOffset == this.endOffset;
    }

    /**
     * Determines if the span contains the specified offset.
     *
     * @param offset The offset to check.
     * @return {@code true} when the span contains the specified offset;
     * otherwise, {@code false}.
     */
    public boolean contains(int offset) {
        // @formatter:off
        return this.startOffset <= offset
            && offset < this.endOffset;
        // @formatter:on
    }

    /**
     * Determines if this span contains the specified span completely.
     *
     * @param other The other span to check.
     * @return {@code true} when the span contains the specified span completely;
     * otherwise, {@code false}.
     */
    public boolean contains(Span other) {
        return contains(other.startOffset) && contains(other.endOffset);
    }

    @Override
    public boolean equals(Object other) {
        // @formatter:off
        return other instanceof Span
            && equals((Span)other);
        // @formatter:on
    }

    /**
     * Compares this object and the specified object for equality.
     *
     * @param other The object to compare this object to.
     * @return {@code true} when this object is equal to the specified object;
     * otherwise, {@code false}.
     */
    public boolean equals(Span other) {
        // @formatter:off
        return this.startOffset == other.startOffset
            && this.endOffset == other.endOffset;
        // @formatter:on
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.startOffset, this.endOffset);
    }

    @Override
    public String toString() {
        return this.startOffset + "-" + this.endOffset;
    }

}
