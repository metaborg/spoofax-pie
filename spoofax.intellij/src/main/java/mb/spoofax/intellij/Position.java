package mb.spoofax.intellij;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;


public final class Position implements Serializable, Comparable<Position> {

    private final int line;
    private final int character;

    public Position(int line, int character) {
        this.line = line;
        this.character = character;
    }

    public int getLine() {
        return line;
    }

    public int getCharacter() {
        return character;
    }

    @Override
    public int compareTo(@NotNull Position other) {
        int comparison = 0;
        if (comparison == 0) comparison = Integer.compare(this.line, other.line);
        if (comparison == 0) comparison = Integer.compare(this.character, other.character);
        return comparison;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Position
            && equals((Position)other);
    }

    public boolean equals(Position other) {
        return this.line == other.line
            && this.character == other.character;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.line, this.character);
    }

    @Override
    public String toString() {
        return this.line + ":" + this.character;
    }

}
