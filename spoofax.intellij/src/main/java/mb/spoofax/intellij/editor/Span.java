package mb.spoofax.intellij.editor;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Objects;


public final class Span implements Serializable {

    public static Span fromLength(Offset startOffset, int length) {
        if (startOffset.compareTo(new Offset(0)) < 0)
            throw new IllegalArgumentException("The start offset $startOffset must be positive or zero.");
        if (length < 0)
            throw new IllegalArgumentException("The length $length must be positive or zero.");
        return new Span(startOffset, new Offset(startOffset.getValue() + length));
    }

    private final Offset startOffset;
    private final Offset endOffset;

    public Span(Offset startOffset, Offset endOffset) {
        if (startOffset.compareTo(new Offset(0)) < 0)
            throw new IllegalArgumentException("The start offset $startOffset must be positive or zero.");
        if (endOffset.compareTo(startOffset) < 0)
            throw new IllegalArgumentException("The end offset $endOffset must be at or after the start offset $startOffset.");
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    public Offset getStartOffset() {
        return startOffset;
    }

    public Offset getEndOffset() {
        return endOffset;
    }

    public long getLength() {
        return this.endOffset.getValue() - this.startOffset.getValue();
    }

    public boolean isEmpty() {
        return this.startOffset.equals(this.endOffset);
    }


    @Override
    public boolean equals(Object other) {
        return other instanceof Span
            && equals((Span)other);
    }

    public boolean equals(Span other) {
        return Objects.equals(this.startOffset, other.startOffset)
            && Objects.equals(this.endOffset, other.endOffset);
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
