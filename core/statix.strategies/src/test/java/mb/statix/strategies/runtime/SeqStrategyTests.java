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
 * Tests the {@link SeqStrategy} class.
 */
@SuppressWarnings({"PointlessArithmeticExpression", "ArraysAsListWithZeroOrOneArgument"}) public final class SeqStrategyTests {

    @Test
    public void shouldApplySecondStrategyToFirstResults() throws InterruptedException {
        // Arrange
        final SeqStrategy<Object, Integer, Integer, Integer> strategy = SeqStrategy.getInstance();
        final TestListStrategy<Integer, Integer> s1 = new TestListStrategy<>(it -> Arrays.asList(it + 1, it + 2, it + 3));
        final TestListStrategy<Integer, Integer> s2 = new TestListStrategy<>(it -> Arrays.asList(it * 1, it * 2, it * 3));

        // Act
        final Seq<Integer> result = strategy.eval(new Object(), s1, s2, 0);

        // Assert
        assertEquals(Arrays.asList(1, 2, 3, 2, 4, 6, 3, 6, 9), result.collect(Collectors.toList()));
    }

    @Test
    public void shouldEvaluateToEmptySequence_whenSecondSequenceIsEmpty() throws InterruptedException {
        // Arrange
        final SeqStrategy<Object, Integer, Integer, Integer> strategy = SeqStrategy.getInstance();
        final TestListStrategy<Integer, Integer> s1 = new TestListStrategy<>(it -> Arrays.asList(it + 1, it + 2, it + 3));
        final TestListStrategy<Integer, Integer> s2 = new TestListStrategy<>(it -> Arrays.asList());

        // Act
        final Seq<Integer> result = strategy.eval(new Object(), s1, s2, 0);

        // Assert
        assertEquals(Arrays.asList(), result.collect(Collectors.toList()));
    }

    @Test
    public void shouldEvaluateToEmptySequence_whenFirstSequenceIsEmpty() throws InterruptedException {
        // Arrange
        final SeqStrategy<Object, Integer, Integer, Integer> strategy = SeqStrategy.getInstance();
        final TestListStrategy<Integer, Integer> s1 = new TestListStrategy<>(it -> Arrays.asList());
        final TestListStrategy<Integer, Integer> s2 = new TestListStrategy<>(it -> Arrays.asList(it * 1, it * 2, it * 3));

        // Act
        final Seq<Integer> result = strategy.eval(new Object(), s1, s2, 0);

        // Assert
        assertEquals(Arrays.asList(), result.collect(Collectors.toList()));
    }

    @Test
    public void shouldEvaluateToEmptySequence_whenBothSequencesAreEmpty() throws InterruptedException {
        // Arrange
        final SeqStrategy<Object, Integer, Integer, Integer> strategy = SeqStrategy.getInstance();
        final TestListStrategy<Integer, Integer> s1 = new TestListStrategy<>(it -> Arrays.asList());
        final TestListStrategy<Integer, Integer> s2 = new TestListStrategy<>(it -> Arrays.asList());

        // Act
        final Seq<Integer> result = strategy.eval(new Object(), s1, s2, 0);

        // Assert
        assertEquals(Arrays.asList(), result.collect(Collectors.toList()));
    }

    @Test
    public void shouldEvaluateSequenceLazy() throws InterruptedException {
        // Arrange
        final SeqStrategy<Object, Integer, Integer, Integer> strategy = SeqStrategy.getInstance();
        final TestListStrategy<Integer, Integer> s1 = new TestListStrategy<>(it -> Arrays.asList(it + 1, it + 2, it + 3));
        final TestListStrategy<Integer, Integer> s2 = new TestListStrategy<>(it -> Arrays.asList(it * 1, it * 2, it * 3));

        // Act/Assert
        final Seq<Integer> result = strategy.eval(new Object(), s1, s2, 0);

        assertTrue(result.next());
        assertEquals(1, s1.nextCalls.get());    // first element of s1
        assertEquals(1, s2.nextCalls.get());    // first element of s2

        assertTrue(result.next());
        assertEquals(1, s1.nextCalls.get());    // first element of s1
        assertEquals(2, s2.nextCalls.get());    // second element of s2

        assertTrue(result.next());
        assertEquals(1, s1.nextCalls.get());    // first element of s1
        assertEquals(3, s2.nextCalls.get());    // third element of s2

        assertTrue(result.next());
        assertEquals(2, s1.nextCalls.get());    // second element of s1
        assertEquals(5, s2.nextCalls.get());    // s2 is empty; first element of s2

        assertTrue(result.next());
        assertEquals(2, s1.nextCalls.get());    // second element of s1
        assertEquals(6, s2.nextCalls.get());    // second element of s2

        assertTrue(result.next());
        assertEquals(2, s1.nextCalls.get());    // second element of s1
        assertEquals(7, s2.nextCalls.get());    // third element of s2

        assertTrue(result.next());
        assertEquals(3, s1.nextCalls.get());    // third element of s1
        assertEquals(9, s2.nextCalls.get());    // s2 is empty; first element of s2

        assertTrue(result.next());
        assertEquals(3, s1.nextCalls.get());    // third element of s1
        assertEquals(10, s2.nextCalls.get());   // second element of s2

        assertTrue(result.next());
        assertEquals(3, s1.nextCalls.get());    // third element of s1
        assertEquals(11, s2.nextCalls.get());   // third element of s2

        assertFalse(result.next());
        assertEquals(4, s1.nextCalls.get());    // s1 is empty
        assertEquals(12, s2.nextCalls.get());   // s2 is empty
    }
}
