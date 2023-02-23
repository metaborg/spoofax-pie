package mb.tego.strategies3.runtime;

import mb.tego.sequences.Seq;
import mb.tego.sequences.SeqBase;
import mb.tego.strategies3.NamedStrategy2;
import mb.tego.strategies3.Strategy;
import mb.tego.utils.ExcludeFromJacocoGeneratedReport;

/**
 * Sequence strategy.
 * <p>
 * This takes two strategies, applying the first to the input and the second to each of the results from the first.
 *
 * @param <T> the type of input (contravariant)
 * @param <U> the type of intermediate (invariant)
 * @param <R> the type of output (covariant)
 */
public final class SeqStrategy<T, U, R> extends NamedStrategy2<Strategy<T, U>, Strategy<U, R>, T, R> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final SeqStrategy instance = new SeqStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static <T, U, R> SeqStrategy<T, U, R> getInstance() { return (SeqStrategy<T, U, R>)instance; }

    private SeqStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    public static <T, U, R> Seq<R> eval(TegoEngine engine, Strategy<T, U> s1, Strategy<U, R> s2, T input) {
        final Seq<U> r1 = engine.eval(s1, input);
        return new SeqBase<R>() {

            // Implementation if `yield` and `yieldBreak` could actually suspend computation
            @SuppressWarnings("unused")
            @ExcludeFromJacocoGeneratedReport
            private void computeNextCoroutine() throws InterruptedException {
                // 0:
                // 1:
                while (r1.next()) {
                    // 2:
                    final U u = r1.getCurrent();
                    final Seq<R> r2 = engine.eval(s2, u);
                    // 3:
                    while (r2 != null && r2.next()) {
                        // 4:
                        final R r = r2.getCurrent();
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
            private Seq<R> r2;

            @Override
            protected void computeNext() throws InterruptedException {
                while (true) {
                    switch (state) {
                        case 0:
                            this.state = 1;
                            continue;
                        case 1:
                            if (!r1.next()) {
                                this.state = 7;
                                continue;
                            }
                            this.state = 2;
                            continue;
                        case 2:
                            final U u = r1.getCurrent();
                            r2 = engine.eval(s2, u);
                            this.state = 3;
                            continue;
                        case 3:
                            if (r2 == null || !r2.next()) {
                                this.state = 6;
                                continue;
                            }
                            this.state = 4;
                            continue;
                        case 4:
                            //noinspection ConstantConditions
                            final R r = r2.getCurrent();
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
    public Seq<R> evalInternal(TegoEngine engine, Strategy<T, U> s1, Strategy<U, R> s2, T input) {
        return eval(engine, s1, s2, input);
    }

    @Override
    public String getName() {
        return "seq";
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches") @Override
    public String getParamName(int index) {
        switch (index) {
            case 0: return "s1";
            case 1: return "s2";
            default: return super.getParamName(index);
        }
    }

    /**
     * A builder for flat maps of strategies.
     */
    @SuppressWarnings("unused")
    public static class Builder<T, U> {

        private final Strategy<T, U> s;

        public Builder(Strategy<T, U> s) {
            this.s = s;
        }

        public <R> SeqStrategy.Builder<T, R> $(Strategy<U, R> s) {
            return new SeqStrategy.Builder<>(SeqStrategy.<T, U, R>getInstance().apply(this.s, s));
        }

        public Strategy<T, U> $() {
            return s;
        }

    }

}
