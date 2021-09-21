package mb.statix.strategies.runtime;

import mb.statix.sequences.Seq;
import mb.statix.strategies.LambdaStrategy1;
import mb.statix.strategies.Strategy;
import mb.statix.strategies.Strategy1;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Function;

/**
 * Strategy convenience functions.
 */
public final class Strategies {
    private Strategies() { /* Prevent instantiation. */ }

    /**
     * Builds a recursive strategy.
     *
     * @param f the strategy builder function, which takes a reference to the built strategy itself
     * @param <CTX> the type of context (invariant)
     * @param <T> the type of input (contravariant)
     * @param <R> the type of output (covariant)
     * @return the resulting strategy
     */
    public static <CTX, T, R> Strategy<CTX, T, R> rec(Function<Strategy<CTX, T, R>, Strategy<CTX, T, R>> f) {
        return new Strategy<CTX, T, R>() {
            @Override
            public @Nullable R evalInternal(TegoEngine engine, CTX ctx, T input) {
                return engine.eval(f.apply(this), ctx, input);
            }
        };
    }

    /**
     * Asserts that the value is not null.
     *
     * @param r the return value
     * @param <R> the type of return value
     * @return the return value
     */
    public static <R> R nn(@Nullable R r) {
        assert r != null : "Value is not supposed to be null";
        return r;
    }

    public static <CTX, I, O> SeqStrategy.Builder<CTX, I, O> seq(
        Strategy<CTX, I, O> s
    ) {
        return new SeqStrategy.Builder<>(s);
    }

    public static <CTX, I, O> Strategy<CTX, Seq<I>, Seq<O>> flatMap(
        Strategy<CTX, I, Seq<O>> s
    ) {
        return FlatMapStrategy.<CTX, I, O>getInstance().apply(s);
    }

    public static <CTX, I, O> Strategy<CTX, I, Seq<O>> single(
        Strategy<CTX, I, Seq<O>> s
    ) {
        return SingleStrategy.<CTX, I, O>getInstance().apply(s);
    }

    public static <CTX, T> Strategy<CTX, T, Seq<T>> try_(
        Strategy<CTX, T, Seq<T>> s
    ) {
        return TryStrategy.<CTX, T>getInstance().apply(s);
    }

    public static <CTX, T> Strategy<CTX, T, Seq<T>> repeat(
        Strategy<CTX, T, Seq<T>> s
    ) {
        return RepeatStrategy.<CTX, T>getInstance().apply(s);
    }

    public static <CTX, I, O> Strategy<CTX, I, Seq<O>> limit(
        int limit,
        Strategy<CTX, I, Seq<O>> s
    ) {
        return LimitStrategy.<CTX, I, O>getInstance().apply(s, limit);
    }

    public static <CTX, I, O> Strategy<CTX, I, Seq<O>> or(
        Strategy<CTX, I, Seq<O>> s1,
        Strategy<CTX, I, Seq<O>> s2
    ) {
        return OrStrategy.<CTX, I, O>getInstance().apply(s1, s2);
    }

    public static <CTX, T> Strategy<CTX, T, Seq<T>> fixSet(
        Strategy<CTX, T, Seq<T>> s
    ) {
        return FixSetStrategy.<CTX, T>getInstance().apply(s);
    }

    public static <CTX, T, R> Strategy<CTX, T, Seq<R>> ntl(
        Strategy<CTX, T, @Nullable R> s
    ) {
        return NullableToListStrategy.<CTX, T, R>getInstance().apply(s);
    }

    public static <CTX, T, R> Strategy<CTX, T, @Nullable T> not(
        Strategy<CTX, T, @Nullable R> s
    ) {
        return NotStrategy.<CTX, T, R>getInstance().apply(s);
    }

    public static <CTX, I, O> Strategy<CTX, I, O> fail() {
        return FailStrategy.getInstance();
    }

    public static <CTX, T> Strategy<CTX, T, T> id() {
        return IdStrategy.getInstance();
    }

    public static <CTX, I, M, O> Strategy<CTX, I, Seq<O>> if_(
        Strategy<CTX, I, Seq<M>> condition,
        Strategy<CTX, M, Seq<O>> onSuccess,
        Strategy<CTX, I, Seq<O>> onFailure
    ) {
        return GlcStrategy.<CTX, I, M, O>getInstance().apply(condition, onSuccess, onFailure);
    }

}
