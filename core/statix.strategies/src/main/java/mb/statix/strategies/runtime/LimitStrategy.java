package mb.statix.strategies.runtime;

import mb.statix.sequences.Seq;
import mb.statix.sequences.SeqBase;
import mb.statix.strategies.NamedStrategy2;
import mb.statix.strategies.Strategy;
import mb.statix.utils.ExcludeFromJacocoGeneratedReport;

/**
 * Limiting strategy.
 *
 * This returns at most the number of elements specified in the limit.
 *
 * @param <CTX> the type of context (invariant)
 * @param <T> the type of input (contravariant)
 * @param <R> the type of output (covariant)
 */
public final class LimitStrategy<CTX, T, R> extends NamedStrategy2<CTX, Strategy<CTX, T, R>, Integer, T, R> {

    @SuppressWarnings("rawtypes")
    private static final LimitStrategy instance = new LimitStrategy();
    @SuppressWarnings("unchecked")
    public static <CTX, T, R> LimitStrategy<CTX, T, R> getInstance() { return (LimitStrategy<CTX, T, R>)instance; }

    private LimitStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    @Override
    public Seq<R> eval(CTX ctx, Strategy<CTX, T, R> s, Integer n, T input) {
        return new SeqBase<R>() {
            // Implementation if `yield` and `yieldBreak` could actually suspend computation
            @SuppressWarnings("unused")
            @ExcludeFromJacocoGeneratedReport
            private void computeNextCoroutine() throws InterruptedException {
                // 0:
                final Seq<R> s1Seq = s.eval(ctx, input);
                // 1:
                while (remaining > 0 && s1Seq.next()) {
                    this.remaining -= 1;
                    this.yield(s1Seq.getCurrent());
                    // 2:
                }
                // 3:
                yieldBreak();
            }

            // STATE MACHINE
            private int state = 0;
            // LOCAL VARIABLES
            private Seq<R> s1Seq;
            private int remaining = n;

            @Override
            protected void computeNext() throws InterruptedException {
                while (true) {
                    switch (state) {
                        case 0:
                            s1Seq = s.eval(ctx, input);
                            this.state = 1;
                            continue;
                        case 1:
                            if (remaining <= 0 || !s1Seq.next()) {
                                this.state = 3;
                                continue;
                            }
                            this.remaining -= 1;
                            this.yield(s1Seq.getCurrent());
                            this.state = 2;
                            return;
                        case 2:
                            this.state = 1;
                            continue;
                        case 3:
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
    public String getName() {
        return "limit";
    }

    @Override
    public String getParamName(int index) {
        switch (index) {
            case 0: return "s";
            case 1: return "n";
            default: return super.getParamName(index);
        }
    }
}
