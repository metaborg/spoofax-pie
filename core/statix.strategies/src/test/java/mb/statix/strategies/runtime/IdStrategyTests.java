package mb.statix.strategies.runtime;

import mb.statix.sequences.Seq;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the {@link IdStrategy} class.
 */
public final class IdStrategyTests {

    @Test
    public void shouldReturnComputationOfInput() throws InterruptedException {
        // Arrange
        final IdStrategy<Object, String> strategy = IdStrategy.getInstance();
        final String input = "My input";

        // Act
        final Seq<String> result = strategy.eval(new Object(), input);

        // Assert
        assertTrue(result.next());
        assertEquals(input, result.getCurrent());
        assertFalse(result.next());
    }

}
