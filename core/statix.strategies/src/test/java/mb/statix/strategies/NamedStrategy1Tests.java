package mb.statix.strategies;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Tests the {@link NamedStrategy1} class.
 */
public final class NamedStrategy1Tests {

    @Test
    public void isAnonymous_shouldReturnFalse() {
        // Arrange
        final MyTestStrategy1 strategy = new MyTestStrategy1();

        // Act
        final boolean anonymous = strategy.isAnonymous();

        // Assert
        assertFalse(anonymous);
    }

    @Test
    public void writeTo_shouldWriteNameToStringBuilder() {
        // Arrange
        final MyTestStrategy1 strategy = new MyTestStrategy1();
        final StringBuilder sb = new StringBuilder();

        // Act
        strategy.writeTo(sb);

        // Assert
        assertEquals("my-test-strategy-1", sb.toString());
    }

    @Test
    public void toString_shouldReturnName() {
        // Arrange
        final MyTestStrategy1 strategy = new MyTestStrategy1();

        // Act
        final String result = strategy.toString();

        // Assert
        assertEquals("my-test-strategy-1", result);
    }

}
