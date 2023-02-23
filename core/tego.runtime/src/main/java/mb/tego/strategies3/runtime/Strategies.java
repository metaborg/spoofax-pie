package mb.tego.strategies3.runtime;

import mb.tego.sequences.Seq;
import mb.tego.strategies3.Strategy;
import mb.tego.strategies3.Strategy1;

/**
 * Strategy convenience functions.
 */
public final class Strategies {
    private Strategies() { /* Prevent instantiation. */ }

    /** Returns a sequence with only distinct elements from the original sequence. */
    public static <T, R> Strategy<T, R> distinct(
        Strategy<T, R> s
    ) {
        return DistinctStrategy.<T, R>getInstance().apply(s);
    }

    /** Starts to build a sequence of strategies. */
    public static <T, R> SeqStrategy.Builder<T, R> seq(
        Strategy<T, R> s
    ) {
        return new SeqStrategy.Builder<>(s);
    }

    /** Casts the input term to the specified type, if possible. */
    public static <T, R> Strategy<T, R> is(
        Class<R> cls
    ) {
        return IsStrategy.<T, R>getInstance().apply(cls);
    }

    /** Returns the input term if the specified strategy succeeds; otherwise, fails. */
    public static <T, R> Strategy<T, T> where(
        Strategy<T, R> s
    ) {
        return WhereStrategy.<T, R>getInstance().apply(s);
    }

    /** Flattens the result of the specified strategy returning a sequence of sequence into a single sequence. */
    public static <T, R> Strategy<T, R> flatten(
        Strategy<T, Seq<R>> s
    ) {
        return FlattenStrategy.<T, R>getInstance().apply(s);
    }

    /** Returns the only value in the returned sequence; otherwise, fails if there are no values or more than one. */
    public static <T, R> Strategy<T, R> single(
        Strategy<T, R> s
    ) {
        return SingleStrategy.<T, R>getInstance().apply(s);
    }

    /** Returns the first value in the returned sequence; otherwise, fails if there are no values. */
    public static <T, R> Strategy<T, R> first(
        Strategy<T, R> s
    ) {
        return FirstStrategy.<T, R>getInstance().apply(s);
    }

    /** Applies the specified strategy to the input term, and returns the resulting term if the strategy succeeds; otherwise, returns the input term if the strategy failed. */
    public static <T> Strategy<T, T> try_(
        Strategy<T, T> s
    ) {
        return TryStrategy.<T>getInstance().apply(s);
    }

    /** Repeatedly applies the specified strategy to the input term, and returns the last term once the strategy fails. */
    public static <T> Strategy<T, T> repeat(
        Strategy<T, T> s
    ) {
        return RepeatStrategy.<T>getInstance().apply(s);
    }

    /** Returns only the first specified number of elements in a sequence. */
    public static <T, R> Strategy<T, R> limit(
        int limit,
        Strategy<T, R> s
    ) {
        return LimitStrategy.<T, R>getInstance().apply(s, limit);
    }

    /** Evaluates two strategies on the input term and returns their results concatenated if both strategies succeed. */
    public static <T, R> Strategy<T, R> and(
        Strategy<T, R> s1,
        Strategy<T, R> s2
    ) {
        return AndStrategy.<T, R>getInstance().apply(s1, s2);
    }

    /** Evaluates two strategies on the input term and returns their results concatenated if either strategy succeeds. */
    public static <T, R> Strategy<T, R> or(
        Strategy<T, R> s1,
        Strategy<T, R> s2
    ) {
        return OrStrategy.<T, R>getInstance().apply(s1, s2);
    }

    /** Repeatedly applies the specified strategy to the input term, and returns the last term once the strategy fails or the output no longer changes. */
    public static <T> Strategy<T, T> fixSet(
        Strategy<T, T> s
    ) {
        return FixSetStrategy.<T>getInstance().apply(s);
    }

    /** Executes a strategy on each element in the specified sequence and concatenates those sequences. */
    public static <A, T, R> Strategy<T, R> flatMap(
        Seq<A> as,
        Strategy1<A, T, R> s
    ) {
        return FlatMapStrategy.<A, T, R>getInstance().apply(as, s);
    }

    /** If the specified strategy succeeds, this strategy fails. Otherwise, it returns the input term. */
    public static <T, R> Strategy<T, T> not(
        Strategy<T, R> s
    ) {
        return NotStrategy.<T, R>getInstance().apply(s);
    }

    /** This strategy always fails. */
    public static <T, R> Strategy<T, R> fail() {
        return FailStrategy.getInstance();
    }

    /** This strategy always returns the input term. */
    public static <T> Strategy<T, T> id() {
        return IdStrategy.getInstance();
    }

    /** If the condition succeeds, applies the first strategy to the input; otherwise, applies the second strategy to the input. */
    public static <T, U, R> Strategy<T, R> if_(
        Strategy<T, U> condition,
        Strategy<T, R> onSuccess,
        Strategy<T, R> onFailure
    ) {
        return IfStrategy.<T, U, R>getInstance().apply(condition, onSuccess, onFailure);
    }

    /** If the condition succeeds, applies the first strategy to the result; otherwise, applies the second strategy to the input. */
    public static <T, U, R> Strategy<T, R> glc(
        Strategy<T, U> condition,
        Strategy<U, R> onSuccess,
        Strategy<T, R> onFailure
    ) {
        return GlcStrategy.<T, U, R>getInstance().apply(condition, onSuccess, onFailure);
    }

}
