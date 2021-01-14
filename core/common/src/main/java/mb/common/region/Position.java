package mb.common.region;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Objects;

/**
 * A position in a document, identified by a line number and character offset.
 */
public final class Position implements Serializable {

    /**
     * Creates a new instance of the {@link Position} class
     * by parsing it from the specified string.
     *
     * @param str the string to parse, in the format "line:character"
     * @return the resulting position; or {@code null} when parsing failed
     */
    public static @Nullable Position fromString(String str) {
        try {
            final int split = str.indexOf(':');
            if(split < 0) return null;
            final String lineStr = str.substring(0, split);
            final int line = Integer.parseInt(lineStr);
            final String characterStr = str.substring(split + 1);
            final int character = Integer.parseInt(characterStr);
            return new Position(line, character);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Creates a new instance of the {@link Position} class.
     *
     * @param line the one-based line number
     * @param character the one-based character offset on the line
     */
    public static Position fromLineChar(int line, int character) {
        return new Position(line, character);
    }

    /** The one-based line number. */
    private final int line;
    /** The one-based character offset on the line. */
    private final int character;

    /**
     * Initializes a new instance of the {@link Position} class.
     *
     * @param line the one-based line number
     * @param character the one-based character offset on the line
     */
    private Position(int line, int character) {
        if (line < 1) throw new IllegalArgumentException("The line number must be positive and greater than zero, got " + line + ".");
        if (character < 1) throw new IllegalArgumentException("The character offset must be positive and greater than zero, got " + character + ".");

        this.line = line;
        this.character = character;
    }

    /**
     * Gets the line number.
     *
     * @return the one-based line number
     */
    public int getLine() {
        return line;
    }

    /**
     * Gets the character offset on the line.
     *
     * @return the one-based character offset on the line
     */
    public int getCharacter() {
        return character;
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        Position position = (Position)o;
        // @formatter:off
        return line == position.line
            && character == position.character;
        // @formatter:on
    }

    @Override
    public int hashCode() {
        return Objects.hash(line, character);
    }

    @Override public String toString() {
        return line + ":" + character;
    }

}
