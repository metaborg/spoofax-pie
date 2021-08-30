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
 * Tests the {@link RepeatStrategy} class.
 */
@SuppressWarnings({"PointlessArithmeticExpression", "ArraysAsListWithZeroOrOneArgument"})
public final class FixSetStrategyTests {

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
        final FixSetStrategy<Object, String> strategy = FixSetStrategy.getInstance();
        final TestListStrategy<String, String> s = new TestListStrategy<>(it -> scoreString(it) < 5 ? Arrays.asList(it + "A", it + "B", it + "C") : Arrays.asList());

        // Act
        final Seq<String> result = strategy.eval(new Object(), s, "A");

        // Assert
        assertEquals(Arrays.asList(
            "AAAAA",
            "AAAAB",
            "AAAAC",
            "AAAB",
            "AAAC",
            "AABA",
            "AABB",
            "AABC",
            "AAC",
            "ABAA",
            "ABAB",
            "ABAC",
            "ABB",
            "ABC",
            "ACA",
            "ACB",
            "ACC"
        ), result.collect(Collectors.toList()));
    }

    @Test
    public void shouldApplyStrategy_untilStrategyReturnsVisitedElements() throws InterruptedException {
        // Arrange
        final FixSetStrategy<Object, String> strategy = FixSetStrategy.getInstance();
        final TestListStrategy<String, String> s = new TestListStrategy<>(it -> scoreString(it) < 5 ? Arrays.asList(it + "A", it + "B", it + "C") : Arrays.asList(it, it));

        // Act
        final Seq<String> result = strategy.eval(new Object(), s, "A");

        // Assert
        assertEquals(Arrays.asList(
            "AAAAA",
            "AAAAB",
            "AAAAC",
            "AAAB",
            "AAAC",
            "AABA",
            "AABB",
            "AABC",
            "AAC",
            "ABAA",
            "ABAB",
            "ABAC",
            "ABB",
            "ABC",
            "ACA",
            "ACB",
            "ACC"
        ), result.collect(Collectors.toList()));
    }

    @Test
    public void shouldApplyStrategy_untilResultsNoLongerChange() throws InterruptedException {
        // Arrange
        final FixSetStrategy<Object, String> strategy = FixSetStrategy.getInstance();
        final TestListStrategy<String, String> s = new TestListStrategy<>(it -> it.equals("B") ? Arrays.asList("A", "B") : Arrays.asList(it));

        // Act
        final Seq<String> result = strategy.eval(new Object(), s, "B");

        // Assert
        assertEquals(Arrays.asList(
            "A",
            "B"
        ), result.collect(Collectors.toList()));
    }

    @Test
    public void shouldEvaluateSequenceLazy() throws InterruptedException {
        // Arrange
        final FixSetStrategy<Object, String> strategy = FixSetStrategy.getInstance();
        final TestListStrategy<String, String> s = new TestListStrategy<>(it -> scoreString(it) < 5 ? Arrays.asList(it + "A", it + "B") : Arrays.asList(it));

        // Act
        final Seq<String> result = strategy.eval(new Object(), s, "A");
        assertEquals(0, s.evalCalls.get());         // not called yet
        assertEquals(0, s.nextCalls.get());         // not called yet

        assertTrue(result.next());
        assertEquals("AAAAA", result.getCurrent());
        //                            ["AA", "AB"]
        //                    ["AAA", "AAB"]["AB"]
        //           ["AAAA", "AAAB"]["AAB"]["AB"]
        // ["AAAAA", "AAAAB"]["AAAB"]["AAB"]["AB"]
        //          ["AAAAB"]["AAAB"]["AAB"]["AB"] -> "AAAAA"
        assertEquals(5, s.evalCalls.get());
        assertEquals(5, s.nextCalls.get());

        assertTrue(result.next());
        assertEquals("AAAAB", result.getCurrent());
        //          ["AAAAB"]["AAAB"]["AAB"]["AB"]
        //          ["AAAAB"]["AAAB"]["AAB"]["AB"]
        //                 []["AAAB"]["AAB"]["AB"] -> "AAAAB"
        assertEquals(6, s.evalCalls.get());
        assertEquals(8, s.nextCalls.get());

        assertTrue(result.next());
        assertEquals("AAAB", result.getCurrent());
        //                 []["AAAB"]["AAB"]["AB"]
        //                   ["AAAB"]["AAB"]["AB"]
        //                   ["AAAB"]["AAB"]["AB"]
        //                         []["AAB"]["AB"] -> "AAAB"
        assertEquals(7, s.evalCalls.get());
        assertEquals(12, s.nextCalls.get());

        assertTrue(result.next());
        assertEquals("AABA", result.getCurrent());
        //                         []["AAB"]["AB"]
        //                           ["AAB"]["AB"]
        //                ["AABA", "AABB"][]["AB"]
        //                ["AABA"]["AABB"][]["AB"]
        //                      []["AABB"][]["AB"] -> "AABA"
        assertEquals(9, s.evalCalls.get());
        assertEquals(17, s.nextCalls.get());

        assertTrue(result.next());
        assertEquals("AABB", result.getCurrent());
        //                      []["AABB"][]["AB"]
        //                        ["AABB"][]["AB"]
        //                              [][]["AB"] -> "AABB"
        assertEquals(10, s.evalCalls.get());
        assertEquals(20, s.nextCalls.get());

        assertTrue(result.next());
        assertEquals("ABAA", result.getCurrent());
        //                              [][]["AB"]
        //                                []["AB"]
        //                                  ["AB"]
        //                        ["ABA", "ABB"][]
        //               ["ABAA", "ABAB"]["ABB"][]
        //               ["ABAA"]["ABAB"]["ABB"][]
        //                     []["ABAB"]["ABB"][] -> "ABAA"
        assertEquals(13, s.evalCalls.get());
        assertEquals(27, s.nextCalls.get());

        assertTrue(result.next());
        assertEquals("ABAB", result.getCurrent());
        //                     []["ABAB"]["ABB"][]
        //                       ["ABAB"]["ABB"][]
        //                             []["ABB"][] -> "ABAB"
        assertEquals(14, s.evalCalls.get());
        assertEquals(30, s.nextCalls.get());

        assertTrue(result.next());
        assertEquals("ABB", result.getCurrent());
        //                             []["ABB"][]
        //                               ["ABB"][]
        //                               ["ABB"][]
        //                                    [][] -> "ABB"
        assertEquals(15, s.evalCalls.get());
        assertEquals(34, s.nextCalls.get());

        assertFalse(result.next());
        //                                    [][]
        //                                      [] -> false
        assertEquals(15, s.evalCalls.get());
        assertEquals(37, s.nextCalls.get());
    }

}
