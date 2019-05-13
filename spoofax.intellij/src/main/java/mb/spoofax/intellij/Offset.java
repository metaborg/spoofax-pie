package mb.spoofax.intellij;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;


public final class Offset implements Comparable<Offset>, Serializable {

    private final long value;

    public Offset(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    @Override
    public int compareTo(@NotNull Offset other) {
        return Long.compare(this.value, other.value);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Offset
            && equals((Offset)other);
    }

    public boolean equals(Offset other) {
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
