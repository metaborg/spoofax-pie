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
        final TegoEngine engine = new TegoRuntimeImpl(null);
        final FailStrategy<String, Integer> strategy = FailStrategy.getInstance();

        // Act
        final Integer result = strategy.evalInternal(engine, "My input");

        // Assert
        assertNull(result);
    }

}
