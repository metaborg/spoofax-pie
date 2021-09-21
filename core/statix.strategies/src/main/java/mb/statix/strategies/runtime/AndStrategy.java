package mb.statix.strategies.runtime;

import mb.statix.sequences.Seq;
import mb.statix.sequences.SeqBase;
import mb.statix.strategies.NamedStrategy2;
import mb.statix.strategies.Strategy;
import mb.statix.utils.ExcludeFromJacocoGeneratedReport;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Conjunction strategy.
 *
 * This evaluates two strategies on the input, and returns the elements of the first sequence
 * and then the elements of the second sequence, but only if both succeed.
 *
 * @param <T> the type of input (contravariant)
 * @param <R> the type of output (covariant)
 */
public final class AndStrategy<T, R> extends NamedStrategy2<Strategy<T, Seq<R>>, Strategy<T, Seq<R>>, T, Seq<R>> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final AndStrategy instance = new AndStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static <T, R> AndStrategy<T, R> getInstance() { return (AndStrategy<T, R>)instance; }

    private AndStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    public static <T, R> Seq<R> eval(TegoEngine engine, Strategy<T, Seq<R>> s1, Strategy<T, Seq<R>> s2, T input) {
        return new SeqBase<R>() {

            // Implementation if `yield` and `yieldBreak` could actually suspend computation
            @SuppressWarnings("unused")
            @ExcludeFromJacocoGeneratedReport
            private void computeNextCoroutine() throws InterruptedException {
                // 0:
                final @Nullable Seq<R> s1Seq = engine.eval(s1, input);
                final @Nullable Seq<R> s2Seq = engine.eval(s2, input);
                if (s1Seq != null && s1Seq.next() && s2Seq != null && s2Seq.next()) {
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
            private @Nullable Seq<R> s1Seq;
            private @Nullable Seq<R> s2Seq;

            @Override
            protected void computeNext() throws InterruptedException {
                while (true) {
                    switch (state) {
                        case 0:
                            s1Seq = engine.eval(s1, input);
                            s2Seq = engine.eval(s2, input);
                            if (s1Seq == null || s2Seq == null || !s1Seq.next() || !s2Seq.next()) {
                                this.state = 8;
                                continue;
                            }
                            this.state = 1;
                            continue;
                        case 1:
                            this.state = 2;
                            continue;
                        case 2:
                            //noinspection ConstantConditions
                            this.yield(s1Seq.getCurrent());
                            this.state = 3;
                            return;
                        case 3:
                            //noinspection ConstantConditions
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
                            //noinspection ConstantConditions
                            this.yield(s2Seq.getCurrent());
                            this.state = 6;
                            return;
                        case 6:
                            //noinspection ConstantConditions
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
    public Seq<R> evalInternal(TegoEngine engine, Strategy<T, Seq<R>> s1, Strategy<T, Seq<R>> s2, T input) {
        return eval(engine, s1, s2, input);
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
