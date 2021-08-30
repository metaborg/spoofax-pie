package mb.statix.strategies.runtime;

import mb.statix.sequences.Seq;
import mb.statix.sequences.SeqBase;
import mb.statix.strategies.NamedStrategy1;
import mb.statix.strategies.Strategy;
import mb.statix.utils.ExcludeFromJacocoGeneratedReport;

/**
 * single(s) strategy.
 *
 * This returns the results of the strategy if it returns exactly one result, otherwise the strategy fails.
 *
 * @param <CTX> the type of context (invariant)
 * @param <T> the type of input (contravariant)
 * @param <R> the type of output (covariant)
 */
public final class SingleStrategy<CTX, T, R> extends NamedStrategy1<CTX, Strategy<CTX, T, R>, T, R> {

    @SuppressWarnings("rawtypes")
    private static final SingleStrategy instance = new SingleStrategy();
    @SuppressWarnings("unchecked")
    public static <CTX, T, R> SingleStrategy<CTX, T, R> getInstance() { return (SingleStrategy<CTX, T, R>)instance; }

    private SingleStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    @Override
    public Seq<R> eval(CTX ctx, Strategy<CTX, T, R> s, T input) {
        return new SeqBase<R>() {
            // Implementation if `yield` and `yieldBreak` could actually suspend computation
            @SuppressWarnings("unused")
            @ExcludeFromJacocoGeneratedReport
            private void computeNextCoroutine() throws InterruptedException {
                // 0:
                final Seq<R> sSeq = s.eval(ctx, input);
                if (sSeq.next()) {
                    final R element = sSeq.getCurrent();
                    if(!sSeq.next()) {
                        // Only one result. Yield it.
                        this.yield(element);
                        // 1:
                    }
                    // 2:
                }
                // 3:
                yieldBreak();
            }

            // STATE MACHINE
            private int state = 0;
            // LOCAL VARIABLES
            // <none>

            @Override
            protected void computeNext() throws InterruptedException {
                while (true) {
                    switch (state) {
                        case 0:
                            final Seq<R> sSeq = s.eval(ctx, input);
                            if (sSeq.next()) {
                                final R element = sSeq.getCurrent();
                                if(!sSeq.next()) {
                                    // Only one result. Yield it.
                                    this.yield(element);
                                    this.state = 1;
                                    return;
                                }
                            }
                            this.state = 3;
                            continue;
                        case 1:
                        case 2:
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
        return "single";
    }

    @Override
    public String getParamName(int index) {
        switch (index) {
            case 0: return "s";
            default: return super.getParamName(index);
        }
    }
}
