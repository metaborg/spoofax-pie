package mb.statix.strategies.runtime;

import mb.statix.sequences.Seq;
import mb.statix.strategies.TestListStrategy;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Tests the {@link SingleStrategy} class.
 */
@SuppressWarnings({"PointlessArithmeticExpression", "ArraysAsListWithZeroOrOneArgument"}) public final class SingleStrategyTests {

    @Test
    public void shouldFail_whenStrategyFails() throws InterruptedException {
        // Arrange
        final SingleStrategy<Object, String, Integer> strategy = SingleStrategy.getInstance();
        final FailStrategy<Object, String, Integer> s = FailStrategy.getInstance();

        // Act
        final Seq<Integer> result = strategy.eval(new Object(), s, "abc");

        // Assert
        assertEquals(Arrays.asList(), result.collect(Collectors.toList()));
    }

    @Test
    public void shouldYieldOneResult_whenStrategyYieldsOneResult() throws InterruptedException {
        // Arrange
        final SingleStrategy<Object, String, Integer> strategy = SingleStrategy.getInstance();
        final TestListStrategy<String, Integer> s = new TestListStrategy<>(it -> Arrays.asList(it.length()));

        // Act
        final Seq<Integer> result = strategy.eval(new Object(), s, "abc");

        // Assert
        assertEquals(Arrays.asList(3), result.collect(Collectors.toList()));
    }

    @Test
    public void shouldFail_whenStrategyYieldsMoreThanOneResult() throws InterruptedException {
        // Arrange
        final SingleStrategy<Object, String, Integer> strategy = SingleStrategy.getInstance();
        final TestListStrategy<String, Integer> s = new TestListStrategy<>(it -> Arrays.asList(it.length(), it.length() + 1));

        // Act
        final Seq<Integer> result = strategy.eval(new Object(), s, "abc");

        // Assert
        assertEquals(Arrays.asList(), result.collect(Collectors.toList()));
    }

    @Test
    public void shouldNotEvaluateMoreThanTwoElements() throws InterruptedException {
        // Arrange
        final SingleStrategy<Object, String, Integer> strategy = SingleStrategy.getInstance();
        final TestListStrategy<String, Integer> s = new TestListStrategy<>(it -> Arrays.asList(it.length(), it.length() + 1));

        // Act
        final Seq<Integer> result = strategy.eval(new Object(), s, "abc");
        assertEquals(0, s.evalCalls.get());        // not yet called
        assertEquals(0, s.nextCalls.get());        // not yet called

        assertFalse(result.next());
        assertEquals(1, s.evalCalls.get());        // called once to get the lazy sequence
        assertEquals(2, s.nextCalls.get());        // called to get the first element, and to see if a second element exists
    }

}
