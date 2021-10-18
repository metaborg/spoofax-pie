package mb.tego.strategies.runtime;

import mb.tego.sequences.Seq;
import mb.tego.strategies.FunctionStrategy;
import mb.tego.strategies.Strategy;
import mb.tego.strategies.TestListStrategy;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.stream.Collectors;

import static mb.tego.strategies.StrategyExt.fun;
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
        final DistinctStrategy<Integer, String> strategy = DistinctStrategy.getInstance();
        final Strategy<Integer, Seq<String>> s = fun(i -> Seq.of(
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
        final @Nullable Seq<String> result = strategy.evalInternal(engine, s, 42);
        assertNotNull(result);

        // Assert
        assertEquals(Arrays.asList(
            "aaa", "aab", "aac", "abb", "abc",
            "acc", "bbb", "bbc", "bcc", "ccc"
        ), result.collect(Collectors.toList()));
    }

}
