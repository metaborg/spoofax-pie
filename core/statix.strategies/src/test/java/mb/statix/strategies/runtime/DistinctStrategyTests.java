package mb.statix.strategies.runtime;

import mb.statix.sequences.Seq;
import mb.statix.strategies.FunctionStrategy;
import mb.statix.strategies.Strategy;
import mb.statix.strategies.TestListStrategy;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static mb.statix.strategies.StrategyExt.fun;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the {@link DistinctStrategy} class.
 */
@SuppressWarnings({"PointlessArithmeticExpression", "ArraysAsListWithZeroOrOneArgument"})
public final class DistinctStrategyTests {

    /** Scores a string by adding the letters. A has value 0, B has value 1, etc. */
    private static int scoreString(String s) {
        int sum = 0;
        for(int i = 0; i < s.length(); i++) {
            sum += (s.charAt(i) - 'A') + 1;
        }
        return sum;
    }

    @Test
    public void shouldApplyStrategy_untilStrategyFails() throws InterruptedException {
        // Arrange
        final TegoEngine engine = new TegoRuntimeImpl(null);
        final DistinctStrategy<Object, Integer, String> strategy = DistinctStrategy.getInstance();
        final Strategy<Object, Integer, Seq<String>> s = fun(i -> Seq.of(
            "aaa", "aab", "aac",
            "aba", "abb", "abc",
            "aca", "acb", "acc",
            "baa", "bab", "bac",
            "bba", "bbb", "bbc",
            "bca", "bcb", "bcc",
            "caa", "cab", "cac",
            "cba", "cbb", "cbc",
            "cca", "ccb", "ccc"
        ).map(v -> v.chars().sorted().mapToObj(i1 -> String.valueOf((char)i1)).collect(Collectors.joining())));

        // Act
        final @Nullable Seq<String> result = strategy.evalInternal(engine, new Object(), s, 42);
        assertNotNull(result);

        // Assert
        assertEquals(Arrays.asList(
            "aaa",
            "aab", "abb", "bbb",
            "aac", "acc", "ccc",
            "bbc", "bcc"
        ), result.collect(Collectors.toList()));
    }

}
