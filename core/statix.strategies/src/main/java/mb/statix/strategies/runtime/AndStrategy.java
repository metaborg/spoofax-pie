package mb.statix.strategies.runtime;

import mb.statix.sequences.Seq;
import mb.statix.sequences.SeqBase;
import mb.statix.strategies.NamedStrategy2;
import mb.statix.strategies.Strategy;
import mb.statix.utils.ExcludeFromJacocoGeneratedReport;

/**
 * Conjunction strategy.
 *
 * This evaluates two strategies on the input, and returns the elements of the first sequence
 * and then the elements of the second sequence, but only if both succeed.
 *
 * @param <CTX> the type of context (invariant)
 * @param <T> the type of input (contravariant)
 * @param <R> the type of output (covariant)
 */
public final class AndStrategy<CTX, T, R> extends NamedStrategy2<CTX, Strategy<CTX, T, R>, Strategy<CTX, T, R>, T, R> {

    @SuppressWarnings("rawtypes")
    private static final AndStrategy instance = new AndStrategy();
    @SuppressWarnings("unchecked")
    public static <CTX, T, R> AndStrategy<CTX, T, R> getInstance() { return (AndStrategy<CTX, T, R>)instance; }

    private AndStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    @Override
    public Seq<R> eval(CTX ctx, Strategy<CTX, T, R> s1, Strategy<CTX, T, R> s2, T input) {
        return new SeqBase<R>() {

            // Implementation if `yield` and `yieldBreak` could actually suspend computation
            @SuppressWarnings("unused")
            @ExcludeFromJacocoGeneratedReport
            private void computeNextCoroutine() throws InterruptedException {
                // 0:
                final Seq<R> s1Seq = s1.eval(ctx, input);
                final Seq<R> s2Seq = s2.eval(ctx, input);
                if (s1Seq.next() && s2Seq.next()) {
                    // 1:
                    do {
                        // 2:
                        this.yield(s1Seq.getCurrent());
                        // 3:
                    } while (s1Seq.next());
                    // 4:
                    do {
                        // 5:
                        this.yield(s2Seq.getCurrent());
                        // 6:
                    } while (s2Seq.next());
                    // 7:
                }
                // 8:
                yieldBreak();
            }

            // STATE MACHINE
            private int state = 0;
            // LOCAL VARIABLES
            private Seq<R> s1Seq;
            private Seq<R> s2Seq;

            @Override
            protected void computeNext() throws InterruptedException {
                while (true) {
                    switch (state) {
                        case 0:
                            s1Seq = s1.eval(ctx, input);
                            s2Seq = s2.eval(ctx, input);
                            if (!s1Seq.next() || !s2Seq.next()) {
                                this.state = 8;
                                continue;
                            }
                            this.state = 1;
                            continue;
                        case 1:
                            this.state = 2;
                            continue;
                        case 2:
                            this.yield(s1Seq.getCurrent());
                            this.state = 3;
                            return;
                        case 3:
                            if (s1Seq.next()) {
                                this.state = 1;
                                continue;
                            }
                            this.state = 4;
                            continue;
                        case 4:
                            this.state = 5;
                            continue;
                        case 5:
                            this.yield(s2Seq.getCurrent());
                            this.state = 6;
                            return;
                        case 6:
                            if (s2Seq.next()) {
                                this.state = 4;
                                continue;
                            }
                            this.state = 7;
                            continue;
                        case 7:
                            this.state = 8;
                            continue;
                        case 8:
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
        return "and";
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
