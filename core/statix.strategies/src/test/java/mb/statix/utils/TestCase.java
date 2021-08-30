package mb.statix.utils;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

/**
 * A test case.
 *
 * @param <T> the type of input
 * @param <R> the type of result
 */
public final class TestCase<T, R> {

    private final @Nullable T input;
    private final @Nullable R expected;

    public static <T, R> TestCase<T, R> of(@Nullable T input, @Nullable R expected) {
        return new TestCase<>(input, expected);
    }

    public TestCase(@Nullable T input, @Nullable R expected) {
        this.input = input;
        this.expected = expected;
    }

    public @Nullable T getInput() {
        return this.input;
    }

    public @Nullable R getExpected() {
        return this.expected;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        TestCase<?, ?> testCase = (TestCase<?, ?>)o;
        return Objects.equals(input, testCase.input)
            && Objects.equals(expected, testCase.expected);
    }

    @Override
    public int hashCode() {
        return Objects.hash(input, expected);
    }

    @Override public String toString() {
        return input + " |-> " + expected;
    }
}
