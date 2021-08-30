package mb.statix.strategies.runtime;

import mb.statix.sequences.Seq;
import mb.statix.sequences.SeqBase;
import mb.statix.strategies.NamedStrategy2;
import mb.statix.strategies.Strategy;
import mb.statix.utils.ExcludeFromJacocoGeneratedReport;

/**
 * Sequence strategy.
 *
 * This evaluates one strategies on the input, and another on the results of the first.
 *
 * @param <CTX> the type of context (invariant)
 * @param <T> the type of input (contravariant)
 * @param <U> the type of intermediate (invariant)
 * @param <R> the type of output (covariant)
 */
public final class SeqStrategy<CTX, T, U, R> extends NamedStrategy2<CTX, Strategy<CTX, T, U>, Strategy<CTX, U, R>, T, R> {

    @SuppressWarnings("rawtypes")
    private static final SeqStrategy instance = new SeqStrategy();
    @SuppressWarnings("unchecked")
    public static <CTX, T, U, R> SeqStrategy<CTX, T, U, R> getInstance() { return (SeqStrategy<CTX, T, U, R>)instance; }

    private SeqStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    @Override
    public Seq<R> eval(CTX ctx, Strategy<CTX, T, U> s1, Strategy<CTX, U, R> s2, T input) {
        return new SeqBase<R>() {

            // Implementation if `yield` and `yieldBreak` could actually suspend computation
            @SuppressWarnings("unused")
            @ExcludeFromJacocoGeneratedReport
            private void computeNextCoroutine() throws InterruptedException {
                // 0:
                final Seq<U> s1Seq = s1.eval(ctx, input);
                // 1:
                while (s1Seq.next()) {
                    // 2:
                    final U u = s1Seq.getCurrent();
                    final Seq<R> s2Seq = s2.eval(ctx, u);
                    // 3:
                    while (s2Seq.next()) {
                        // 4:
                        final R r = s2Seq.getCurrent();
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
            private Seq<U> s1Seq;
            private Seq<R> s2Seq;

            @Override
            protected void computeNext() throws InterruptedException {
                while (true) {
                    switch (state) {
                        case 0:
                            s1Seq = s1.eval(ctx, input);
                            this.state = 1;
                            continue;
                        case 1:
                            if (!s1Seq.next()) {
                                this.state = 7;
                                continue;
                            }
                            this.state = 2;
                            continue;
                        case 2:
                            final U u = s1Seq.getCurrent();
                            s2Seq = s2.eval(ctx, u);
                            this.state = 3;
                            continue;
                        case 3:
                            if (!s2Seq.next()) {
                                this.state = 6;
                                continue;
                            }
                            this.state = 4;
                            continue;
                        case 4:
                            final R r = s2Seq.getCurrent();
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
}
