package mb.statix.strategies.runtime;

import mb.statix.sequences.Seq;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests the {@link IdStrategy} class.
 */
public final class FailStrategyTests {

    @Test
    public void shouldAlwaysFail() throws InterruptedException {
        // Arrange
        final FailStrategy<Object, String, Integer> strategy = FailStrategy.getInstance();

        // Act
        final Seq<Integer> result = strategy.eval(new Object(), "My input");

        // Assert
        assertFalse(result.next());
    }

}
