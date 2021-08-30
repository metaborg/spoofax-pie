package mb.statix.strategies.runtime;

import mb.statix.patterns.TypeOfPattern;
import mb.statix.sequences.Seq;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the {@link MatchStrategy} class.
 */
public final class MatchStrategyTests {

    @Test
    public void shouldCastAndReturnInput_whenPatternMatches() throws InterruptedException {
        // Arrange
        final MatchStrategy<Object, Object, A> strategy = MatchStrategy.getInstance();

        // Act
        final Seq<A> result = strategy.eval(new Object(), new TypeOfPattern<>(A.class), new B());

        // Assert
        assertTrue(result.next());
    }

    @Test
    public void shouldFail_whenPatternDoesNotMatch() throws InterruptedException {
        // Arrange
        final MatchStrategy<Object, Object, A> strategy = MatchStrategy.getInstance();

        // Act
        final Seq<A> result = strategy.eval(new Object(), new TypeOfPattern<>(A.class), new Object());

        // Assert
        assertFalse(result.next());
    }

    public static class A {}
    public static final class B extends A {}

}
