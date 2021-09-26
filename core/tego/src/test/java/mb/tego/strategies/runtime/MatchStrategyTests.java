package mb.tego.strategies.runtime;

import mb.tego.patterns.TypeOfPattern;
import mb.tego.sequences.Seq;
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
        final TegoEngine engine = new TegoRuntimeImpl(null);
        final MatchStrategy<Object, A> strategy = MatchStrategy.getInstance();

        // Act
        final Seq<A> result = strategy.evalInternal(engine, new TypeOfPattern<>(A.class), new B());

        // Assert
        assertTrue(result.next());
    }

    @Test
    public void shouldFail_whenPatternDoesNotMatch() throws InterruptedException {
        // Arrange
        final TegoEngine engine = new TegoRuntimeImpl(null);
        final MatchStrategy<Object, A> strategy = MatchStrategy.getInstance();

        // Act
        final Seq<A> result = strategy.evalInternal(engine, new TypeOfPattern<>(A.class), new Object());

        // Assert
        assertFalse(result.next());
    }

    public static class A {}
    public static final class B extends A {}

}
