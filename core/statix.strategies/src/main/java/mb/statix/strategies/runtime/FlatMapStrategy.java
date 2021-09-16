package mb.statix.strategies.runtime;

import mb.statix.sequences.Seq;
import mb.statix.sequences.SeqBase;
import mb.statix.strategies.NamedStrategy1;
import mb.statix.strategies.NamedStrategy2;
import mb.statix.strategies.Strategy;
import mb.statix.utils.ExcludeFromJacocoGeneratedReport;

/**
 * FlatMap strategy (Monadic bind).
 *
 * @param <CTX> the type of context (invariant)
 * @param <T> the type of input (contravariant)
 * @param <R> the type of output (covariant)
 */
public final class FlatMapStrategy<CTX, T, R> extends NamedStrategy1<CTX, Strategy<CTX, T, Seq<R>>, Seq<T>, Seq<R>> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final FlatMapStrategy instance = new FlatMapStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static <CTX, T, R> FlatMapStrategy<CTX, T, R> getInstance() { return (FlatMapStrategy<CTX, T, R>)instance; }

    private FlatMapStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    public static <CTX, T, R> Seq<R> eval(TegoEngine engine, CTX ctx, Strategy<CTX, T, Seq<R>> s, Seq<T> input) {
        return new SeqBase<R>() {

            // Implementation if `yield` and `yieldBreak` could actually suspend computation
            @SuppressWarnings("unused")
            @ExcludeFromJacocoGeneratedReport
            private void computeNextCoroutine() throws InterruptedException {
                throw new UnsupportedOperationException("Not yet implemented");
            }

            // STATE MACHINE
            private int state = 0;
            // LOCAL VARIABLES
            private Seq<R> sSeq;

            @Override
            protected void computeNext() throws InterruptedException {
                throw new UnsupportedOperationException("Not yet implemented");
            }
        };
    }

    @Override
    public Seq<R> evalInternal(TegoEngine engine, CTX ctx, Strategy<CTX, T, Seq<R>> s, Seq<T> input) {
        return eval(engine, ctx, s, input);
    }

    @Override
    public String getName() {
        return "flatMap";
    }

    @Override
    public String getParamName(int index) {
        switch (index) {
            case 0: return "s";
            default: return super.getParamName(index);
        }
    }
}
