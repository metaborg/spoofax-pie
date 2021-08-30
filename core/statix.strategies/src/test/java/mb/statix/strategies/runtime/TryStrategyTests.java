package mb.statix.strategies.runtime;

import mb.statix.sequences.Seq;
import mb.statix.strategies.TestListStrategy;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the {@link TryStrategy} class.
 */
@SuppressWarnings({"PointlessArithmeticExpression", "ArraysAsListWithZeroOrOneArgument"}) public final class TryStrategyTests {

    @Test
    public void shouldEvaluateToInput_whenStrategyFails() throws InterruptedException {
        // Arrange
        final TryStrategy<Object, Integer> strategy = TryStrategy.getInstance();
        final TestListStrategy<Integer, Integer> s = new TestListStrategy<>(it -> Arrays.asList());

        // Act
        final Seq<Integer> result = strategy.eval(new Object(), s, 42);

        // Assert
        assertEquals(Arrays.asList(42), result.collect(Collectors.toList()));
    }

    @Test
    public void shouldEvaluateToResults_whenStrategySucceeds() throws InterruptedException {
        // Arrange
        final TryStrategy<Object, Integer> strategy = TryStrategy.getInstance();
        final TestListStrategy<Integer, Integer> s = new TestListStrategy<>(it -> Arrays.asList(it + 1, it + 2, it + 3));

        // Act
        final Seq<Integer> result = strategy.eval(new Object(), s, 42);

        // Assert
        assertEquals(Arrays.asList(43, 44, 45), result.collect(Collectors.toList()));
    }

    @Test
    public void shouldEvaluateSequenceLazy() throws InterruptedException {
        // Arrange
        final TryStrategy<Object, Integer> strategy = TryStrategy.getInstance();
        final TestListStrategy<Integer, Integer> s = new TestListStrategy<>(it -> Arrays.asList(it + 1, it + 2, it + 3));

        // Act
        final Seq<Integer> result = strategy.eval(new Object(), s, 42);

        assertTrue(result.next());
        assertEquals(1, s.nextCalls.get());        // called to get the first element

        assertTrue(result.next());
        assertEquals(2, s.nextCalls.get());        // called to get the second element

        assertTrue(result.next());
        assertEquals(3, s.nextCalls.get());        // called to get the third element

        assertFalse(result.next());
        assertEquals(4, s.nextCalls.get());        // called to find the sequence empty

        // Final tally
        assertEquals(1, s.evalCalls.get());
        assertEquals(4, s.nextCalls.get());
    }

}
