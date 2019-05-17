package mb.spoofax.intellij;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;


/**
 * Selects a line-offset based text position.
 */
public final class Position implements Serializable, Comparable<Position> {

    private final int line;
    private final int character;

    /**
     * Initializes a new instance of the {@link Position} class.
     *
     * @param line The one-based line number.
     * @param character The one-based character offset on the line.
     */
    public Position(int line, int character) {
        if (line < 1) throw new IllegalArgumentException("Line number must be 1 or greater.");
        if (character < 1) throw new IllegalArgumentException("Character offsets must be 1 or greater.");

        this.line = line;
        this.character = character;
    }

    /**
     * Gets the one-based line number.
     *
     * @return The line number.
     */
    public int getLine() {
        return line;
    }

    /**
     * Gets the one-based character offset on the line.
     *
     * @return The character offset.
     */
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
        // @formatter:off
        return other instanceof Position
            && equals((Position)other);
        // @formatter:on
    }

    /**
     * Compares this object and the specified object for equality.
     *
     * @param other The object to compare this object to.
     * @return {@code true} when this object is equal to the specified object;
     * otherwise, {@code false}.
     */
    public boolean equals(Position other) {
        // @formatter:off
        return this.line == other.line
            && this.character == other.character;
        // @formatter:on
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
