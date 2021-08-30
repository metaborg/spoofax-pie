package mb.statix.strategies.runtime;

import mb.statix.sequences.Seq;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the {@link ConstStrategy} class.
 */
public final class ConstStrategyTests {

    @Test
    public void shouldReturnArgumentAsResult() throws InterruptedException {
        // Arrange
        final ConstStrategy<Object, Integer, String> strategy = ConstStrategy.getInstance();
        final String arg1 = "My arg";

        // Act
        final Seq<String> result = strategy.eval(new Object(), arg1, 42);

        // Assert
        assertTrue(result.next());
        assertEquals(arg1, result.getCurrent());
        assertFalse(result.next());
    }

}
