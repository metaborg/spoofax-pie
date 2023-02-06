package mb.tego.strategies.runtime;

import mb.tego.sequences.Seq;
import mb.tego.strategies.LambdaStrategy1;
import mb.tego.strategies.Strategy;
import mb.tego.strategies.Strategy1;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Strategy convenience functions.
 */
public final class Strategies {
    private Strategies() { /* Prevent instantiation. */ }

    public static <T, R> Strategy<T, Seq<R>> distinct(
        Strategy<T, Seq<R>> s
    ) {
        return DistinctStrategy.<T, R>getInstance().apply(s);
    }

    public static <I, O> SeqStrategy.Builder<I, O> seq(
        Strategy<I, O> s
    ) {
        return new SeqStrategy.Builder<>(s);
    }

    public static <I, O> Strategy<I, @Nullable O> is(
        Class<O> cls
    ) {
        return IsStrategy.<I, O>getInstance().apply(cls);
    }

    public static <I, O> Strategy<I, @Nullable I> where(
        Strategy<I, O> s
    ) {
        return WhereStrategy.<I, O>getInstance().apply(s);
    }

    public static <I, O> Strategy<Seq<I>, Seq<O>> flatMap(
        Strategy<I, Seq<O>> s
    ) {
        return FlatMapStrategy.<I, O>getInstance().apply(s);
    }

    public static <I, O> Strategy<I, Seq<O>> flatten(
        Strategy<I, Seq<Seq<O>>> s
    ) {
        return FlattenStrategy.<I, O>getInstance().apply(s);
    }

    public static <I, O> Strategy<I, Seq<O>> single(
        Strategy<I, Seq<O>> s
    ) {
        return SingleStrategy.<I, O>getInstance().apply(s);
    }

    public static <I, O> Strategy<I, O> first(
        Strategy<I, Seq<O>> s
    ) {
        return FirstStrategy.<I, O>getInstance().apply(s);
    }

    public static <T> Strategy<T, Seq<T>> try_(
        Strategy<T, Seq<T>> s
    ) {
        return TryStrategy.<T>getInstance().apply(s);
    }

    public static <T> Strategy<T, Seq<T>> repeat(
        Strategy<T, Seq<T>> s
    ) {
        return RepeatStrategy.<T>getInstance().apply(s);
    }

    public static <I, O> Strategy<I, Seq<O>> limit(
        int limit,
        Strategy<I, Seq<O>> s
    ) {
        return LimitStrategy.<I, O>getInstance().apply(s, limit);
    }

    public static <I, O> Strategy<I, Seq<O>> or(
        Strategy<I, Seq<O>> s1,
        Strategy<I, Seq<O>> s2
    ) {
        return OrStrategy.<I, O>getInstance().apply(s1, s2);
    }

    public static <T> Strategy<T, Seq<T>> fixSet(
        Strategy<T, Seq<T>> s
    ) {
        return FixSetStrategy.<T>getInstance().apply(s);
    }

    /**
     * For each element {@code a} in {@code as}, executes the strategy {@code s(a)} on the input and returns a sequence of results,
     * excluding those that returned {@code null}.
     *
     * @param as the elements
     * @param s the strategy
     * @return the sequence of results
     * @param <A> the type of elements
     * @param <T> the type of input
     * @param <R> the type of results
     */
    public static <A, T, R> Strategy<T, Seq<R>> forEach(
        Seq<A> as,
        Strategy1<A, T, R> s
    ) {
        return ForEachStrategy.<A, T, R>getInstance().apply(as, s);
    }

    public static <T, R> Strategy<T, Seq<R>> ntl(
        Strategy<T, @Nullable R> s
    ) {
        return NullableToListStrategy.<T, R>getInstance().apply(s);
    }

    public static <T, R> Strategy<T, @Nullable T> not(
        Strategy<T, @Nullable R> s
    ) {
        return NotStrategy.<T, R>getInstance().apply(s);
    }

    public static <I, O> Strategy<I, O> fail() {
        return FailStrategy.getInstance();
    }

    public static <T> Strategy<T, T> id() {
        return IdStrategy.getInstance();
    }

    public static <I, M, O> Strategy<I, O> if_(
        Strategy<I, @Nullable M> condition,
        Strategy<M, @Nullable O> onSuccess,
        Strategy<I, @Nullable O> onFailure
    ) {
        return IfStrategy.<I, M, O>getInstance().apply(condition, onSuccess, onFailure);
    }

}
