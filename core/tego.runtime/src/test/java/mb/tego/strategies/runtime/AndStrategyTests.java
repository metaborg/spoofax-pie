package mb.tego.strategies.runtime;

import mb.tego.sequences.Seq;
import mb.tego.strategies.TestListStrategy;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the {@link AndStrategy} class.
 */
@SuppressWarnings({"PointlessArithmeticExpression", "ArraysAsListWithZeroOrOneArgument"}) public final class AndStrategyTests {

    @Test
    public void shouldEvaluateFirstSequenceThenSecondSequence() throws InterruptedException {
        // Arrange
        final TegoEngine engine = new TegoRuntimeImpl(null);
        final AndStrategy<Integer, Integer> strategy = AndStrategy.getInstance();
        final TestListStrategy<Integer, Integer> s1 = new TestListStrategy<>(it -> Arrays.asList(it + 1, it + 2, it + 3));
        final TestListStrategy<Integer, Integer> s2 = new TestListStrategy<>(it -> Arrays.asList(it * 1, it * 2, it * 3));

        // Act
        final Seq<Integer> result = strategy.evalInternal(engine, s1, s2, 42);

        // Assert
        assertEquals(Arrays.asList(43, 44, 45, 42, 84, 126), result.collect(Collectors.toList()));
    }

    @Test
    public void shouldEvaluateToEmptySequence_whenSecondSequenceIsEmpty() throws InterruptedException {
        // Arrange
        final TegoEngine engine = new TegoRuntimeImpl(null);
        final AndStrategy<Integer, Integer> strategy = AndStrategy.getInstance();
        final TestListStrategy<Integer, Integer> s1 = new TestListStrategy<>(it -> Arrays.asList(it + 1, it + 2, it + 3));
        final TestListStrategy<Integer, Integer> s2 = new TestListStrategy<>(it -> Arrays.asList());

        // Act
        final Seq<Integer> result = strategy.evalInternal(engine, s1, s2, 42);

        // Assert
        assertEquals(Arrays.asList(), result.collect(Collectors.toList()));
    }

    @Test
    public void shouldEvaluateToEmptySequence_whenFirstSequenceIsEmpty() throws InterruptedException {
        // Arrange
        final TegoEngine engine = new TegoRuntimeImpl(null);
        final AndStrategy<Integer, Integer> strategy = AndStrategy.getInstance();
        final TestListStrategy<Integer, Integer> s1 = new TestListStrategy<>(it -> Arrays.asList());
        final TestListStrategy<Integer, Integer> s2 = new TestListStrategy<>(it -> Arrays.asList(it * 1, it * 2, it * 3));

        // Act
        final Seq<Integer> result = strategy.evalInternal(engine, s1, s2, 42);

        // Assert
        assertEquals(Arrays.asList(), result.collect(Collectors.toList()));
    }

    @Test
    public void shouldEvaluateToEmptySequence_whenBothSequencesAreEmpty() throws InterruptedException {
        // Arrange
        final TegoEngine engine = new TegoRuntimeImpl(null);
        final AndStrategy<Integer, Integer> strategy = AndStrategy.getInstance();
        final TestListStrategy<Integer, Integer> s1 = new TestListStrategy<>(it -> Arrays.asList());
        final TestListStrategy<Integer, Integer> s2 = new TestListStrategy<>(it -> Arrays.asList());

        // Act
        final Seq<Integer> result = strategy.evalInternal(engine, s1, s2, 42);

        // Assert
        assertEquals(Arrays.asList(), result.collect(Collectors.toList()));
    }

    @Test
    public void shouldEvaluateSequenceLazy() throws InterruptedException {
        // Arrange
        final TegoEngine engine = new TegoRuntimeImpl(null);
        final AndStrategy<Integer, Integer> strategy = AndStrategy.getInstance();
        final TestListStrategy<Integer, Integer> s1 = new TestListStrategy<>(it -> Arrays.asList(it + 1, it + 2, it + 3));
        final TestListStrategy<Integer, Integer> s2 = new TestListStrategy<>(it -> Arrays.asList(it * 1, it * 2, it * 3));

        // Act/Assert
        final Seq<Integer> result = strategy.evalInternal(engine, s1, s2, 42);

        assertTrue(result.next());
        assertEquals(1, s1.nextCalls.get());        // called to get the first element
        assertEquals(1, s2.nextCalls.get());        // called to determine whether there is a first element

        assertTrue(result.next());
        assertEquals(2, s1.nextCalls.get());        // called to get the second element
        assertEquals(1, s2.nextCalls.get());        // not called yet

        assertTrue(result.next());
        assertEquals(3, s1.nextCalls.get());        // called to get the third element
        assertEquals(1, s2.nextCalls.get());        // not called yet

        assertTrue(result.next());
        assertEquals(4, s1.nextCalls.get());        // called to find the sequence empty
        assertEquals(1, s2.nextCalls.get());        // not called again, first element was already computed

        assertTrue(result.next());
        assertEquals(2, s2.nextCalls.get());        // called to get the second element

        assertTrue(result.next());
        assertEquals(3, s2.nextCalls.get());        // called to get the third element

        assertFalse(result.next());
        assertEquals(4, s2.nextCalls.get());        // called to find the sequence empty

        // Final tally
        assertEquals(1, s1.evalCalls.get());
        assertEquals(1, s2.evalCalls.get());
        assertEquals(4, s1.nextCalls.get());
        assertEquals(4, s2.nextCalls.get());
    }
}
