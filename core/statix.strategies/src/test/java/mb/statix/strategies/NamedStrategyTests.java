package mb.statix.strategies;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the {@link NamedStrategy} class.
 */
public final class NamedStrategyTests {

    @Test
    public void isAnonymous_shouldReturnFalse() {
        // Arrange
        final MyTestStrategy strategy = new MyTestStrategy();

        // Act
        final boolean anonymous = strategy.isAnonymous();

        // Assert
        assertFalse(anonymous);
    }

    @Test
    public void writeTo_shouldWriteNameToStringBuilder() {
        // Arrange
        final MyTestStrategy strategy = new MyTestStrategy();
        final StringBuilder sb = new StringBuilder();

        // Act
        strategy.writeTo(sb);

        // Assert
        assertEquals("my-test-strategy", sb.toString());
    }

    @Test
    public void toString_shouldReturnName() {
        // Arrange
        final MyTestStrategy strategy = new MyTestStrategy();

        // Act
        final String result = strategy.toString();

        // Assert
        assertEquals("my-test-strategy", result);
    }

}
