package mb.statix.strategies;

import mb.statix.sequences.Seq;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link Strategy} interface.
 */
public final class StrategyTests {
    @Test
    public void getArity_shouldReturn0() {
        // Arrange
        final Strategy<Object, String, Integer> strategy = (o, input) -> Seq.of(input.length());

        // Act
        final int arity = strategy.getArity();

        // Assert
        assertEquals(0, arity);
    }
}
