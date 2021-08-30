package mb.statix.utils;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the {@link StringUtils} class.
 */
public final class StringUtilsTests {

    @Test
    public void escapeJava_shouldReturnNull_whenInputIsNull() {
        // Arrange
        @Nullable final String input = null;

        // Act
        @Nullable final String result = StringUtils.escapeJava(input);

        // Assert
        assertNull(result);
    }

    @Test
    public void escapeJava_shouldReturnEmptyString_whenInputIsEmptyString() {
        // Arrange
        final String input = "";

        // Act
        final String result = StringUtils.escapeJava(input);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    public void escapeJava_shouldReturnInputString_whenThereIsNothingToEscape() {
        // Arrange
        final String input = "foo bar!";

        // Act
        final String result = StringUtils.escapeJava(input);

        // Assert
        assertEquals(input, result);
    }

    @Test
    public void escapeJava_shouldReturnEscaspedString() {
        // Arrange
        final String input = "\"Nothing is impossible,\t\\\b\bthe word itself says,\r\n'I'm possible!'\"";

        // Act
        final String result = StringUtils.escapeJava(input);

        // Assert
        assertEquals("\\\"Nothing is impossible,\\t\\\\\\b\\bthe word itself says,\\r\\n\\'I\\'m possible!\\'\\\"", result);
    }

}
