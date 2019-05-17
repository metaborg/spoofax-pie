package mb.spoofax.intellij;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;


public final class OffsetX implements Comparable<OffsetX>, Serializable {

    private final long value;

    public OffsetX(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    @Override
    public int compareTo(@NotNull OffsetX other) {
        return Long.compare(this.value, other.value);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof OffsetX
            && equals((OffsetX)other);
    }

    public boolean equals(OffsetX other) {
        return this.value == other.value;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(this.value);
    }

    @Override
    public String toString() {
        return Long.toString(this.value);
    }

}
