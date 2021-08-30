package mb.statix.strategies;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests the {@link Writable} interface.
 */
public final class WritableTests {

    @Test
    public void writeTo_shouldAppendTheStringRepresentationToTheStringBuilder() {
        // Arrange
        final Writable writable = new Writable() {
            @Override
            public String toString() {
                return "<Writable string representation>";
            }
        };
        final StringBuilder sb = new StringBuilder();

        // Act
        writable.writeTo(sb);

        // Assert
        assertEquals("<Writable string representation>", sb.toString());
    }

    @Test
    public void writeTo_throwsWhenStringBuilderIsNull() {
        // Arrange
        final Writable writable = new Writable() {};

        // Act/Assert
        assertThrows(NullPointerException.class, () -> {
            writable.writeTo(null);
        });
    }

}
