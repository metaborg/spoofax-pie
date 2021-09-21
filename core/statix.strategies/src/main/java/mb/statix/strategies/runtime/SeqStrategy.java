package mb.statix.strategies.runtime;

import mb.statix.sequences.Seq;
import mb.statix.sequences.SeqBase;
import mb.statix.strategies.NamedStrategy2;
import mb.statix.strategies.Strategy;
import mb.statix.utils.ExcludeFromJacocoGeneratedReport;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Sequence strategy.
 *
 * This evaluates one strategy on the input, and another on the results of the first.
 *
 * @param <CTX> the type of context (invariant)
 * @param <T> the type of input (contravariant)
 * @param <U> the type of intermediate (invariant)
 * @param <R> the type of output (covariant)
 */
public final class SeqStrategy<CTX, T, U, R> extends NamedStrategy2<CTX, Strategy<CTX, T, @Nullable U>, Strategy<CTX, U, @Nullable R>, T, @Nullable R> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final SeqStrategy instance = new SeqStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static <CTX, T, U, R> SeqStrategy<CTX, T, U, R> getInstance() { return (SeqStrategy<CTX, T, U, R>)instance; }

    private SeqStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    @SuppressWarnings("RedundantIfStatement")
    public static <CTX, T, U, R> @Nullable R eval(TegoEngine engine, CTX ctx, Strategy<CTX, T, @Nullable U> s1, Strategy<CTX, U, @Nullable R> s2, T input) {
        final @Nullable U r1 = engine.eval(s1, ctx, input);
        if (r1 == null) return null;
        final @Nullable R r2 = engine.eval(s2, ctx, r1);
        if (r2 == null) return null;
        return r2;
    }

    @Override
    public @Nullable R evalInternal(TegoEngine engine, CTX ctx, Strategy<CTX, T, @Nullable U> s1, Strategy<CTX, U, @Nullable R> s2, T input) {
        return eval(engine, ctx, s1, s2, input);
    }

    @Override
    public String getName() {
        return "seq";
    }

    @Override
    public String getParamName(int index) {
        switch (index) {
            case 0: return "s1";
            case 1: return "s2";
            default: return super.getParamName(index);
        }
    }

    /**
     * A builder for flat maps of strategies.
     *
     * @param <I> the input type
     * @param <M> the output type
     */
    @SuppressWarnings("unused")
    public static class Builder<CTX, I, M> {

        private final Strategy<CTX, I, @Nullable M> s;

        public Builder(Strategy<CTX, I, @Nullable M> s) {
            this.s = s;
        }

        public <O> Builder<CTX, I, @Nullable O> $(Strategy<CTX, M, @Nullable O> s) {
            return new Builder<>(SeqStrategy.<CTX, I, M, O>getInstance().apply(this.s, s));
        }

        public Strategy<CTX, I, @Nullable M> $() {
            return s;
        }

    }

}
