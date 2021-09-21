package mb.statix.strategies.runtime;

import mb.statix.sequences.Seq;
import mb.statix.sequences.SeqBase;
import mb.statix.strategies.NamedStrategy1;
import mb.statix.strategies.NamedStrategy2;
import mb.statix.strategies.Strategy;
import mb.statix.utils.ExcludeFromJacocoGeneratedReport;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * FlatMap strategy.
 *
 * This wraps a strategy such that it can accept a sequence of values,
 * and flat-maps the results.
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

    public static <CTX, T, U, R> Seq<R> eval(TegoEngine engine, CTX ctx, Strategy<CTX, T, Seq<R>> s, Seq<T> input) {
        return new SeqBase<R>() {

            // Implementation if `yield` and `yieldBreak` could actually suspend computation
            @SuppressWarnings("unused")
            @ExcludeFromJacocoGeneratedReport
            private void computeNextCoroutine() throws InterruptedException {
                // 0:
                // 1:
                while (input.next()) {
                    // 2:
                    final T t = input.getCurrent();
                    final @Nullable Seq<R> rs = engine.eval(s, ctx, t);
                    // 3:
                    while (rs != null && rs.next()) {
                        // 4:
                        final R r = rs.getCurrent();
                        this.yield(r);
                        // 5:
                    }
                    // 6:
                }
                // 7:
                yieldBreak();
            }

            // STATE MACHINE
            private int state = 0;
            // LOCAL VARIABLES
            private @Nullable Seq<R> rs;

            @Override
            protected void computeNext() throws InterruptedException {
                while (true) {
                    switch (state) {
                        case 0:
                            this.state = 1;
                            continue;
                        case 1:
                            if (!input.next()) {
                                this.state = 7;
                                continue;
                            }
                            this.state = 2;
                            continue;
                        case 2:
                            final T t = input.getCurrent();
                            rs = engine.eval(s, ctx, t);
                            this.state = 3;
                            continue;
                        case 3:
                            if (rs == null || !rs.next()) {
                                this.state = 6;
                                continue;
                            }
                            this.state = 4;
                            continue;
                        case 4:
                            //noinspection ConstantConditions
                            final R r = rs.getCurrent();
                            this.yield(r);
                            this.state = 5;
                            return;
                        case 5:
                            this.state = 3;
                            continue;
                        case 6:
                            this.state = 1;
                            continue;
                        case 7:
                            yieldBreak();
                            this.state = -1;
                            return;
                        default:
                            throw new IllegalStateException("Illegal state: " + state);
                    }
                }
            }
        };
    }

    @Override
    public Seq<R> evalInternal(TegoEngine engine, CTX ctx, Strategy<CTX, T, Seq<R>> s, Seq<T> input) {
        return eval(engine, ctx, s, input);
    }

    @Override
    public String getName() {
        return "seq";
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches") @Override
    public String getParamName(int index) {
        switch (index) {
            case 0: return "s";
            default: return super.getParamName(index);
        }
    }

}
