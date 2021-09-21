package mb.statix.strategies.runtime;

import mb.statix.sequences.Seq;
import mb.statix.sequences.SeqBase;
import mb.statix.strategies.NamedStrategy2;
import mb.statix.strategies.Strategy;
import mb.statix.utils.ExcludeFromJacocoGeneratedReport;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Disjunction strategy.
 *
 * This evaluates two strategies on the input, and returns the elements of the first sequence
 * and then the elements of the second sequence, but only if at least one succeeds.
 *
 * @param <T> the type of input (contravariant)
 * @param <R> the type of output (covariant)
 */
public final class OrStrategy<T, R> extends NamedStrategy2<Strategy<T, Seq<R>>, Strategy<T, Seq<R>>, T, Seq<R>> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final OrStrategy instance = new OrStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static <T, R> OrStrategy<T, R> getInstance() { return (OrStrategy<T, R>)instance; }

    private OrStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    public static <T, R> Seq<R> eval(TegoEngine engine, Strategy<T, Seq<R>> s1, Strategy<T, Seq<R>> s2, T input) {
        return new SeqBase<R>() {

            // Implementation if `yield` and `yieldBreak` could actually suspend computation
            @SuppressWarnings("unused")
            @ExcludeFromJacocoGeneratedReport
            private void computeNextCoroutine() throws InterruptedException {
                // 0:
                final @Nullable Seq<R> s1Seq = engine.eval(s1, input);
                // 1:
                while (s1Seq != null && s1Seq.next()) {
                    // 2:
                    this.yield(s1Seq.getCurrent());
                    // 3:
                }
                // 4:
                final @Nullable Seq<R> s2Seq = engine.eval(s2, input);
                // 5:
                while (s2Seq != null && s2Seq.next()) {
                    // 6:
                    this.yield(s2Seq.getCurrent());
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
                            this.state = 1;
                            continue;
                        case 1:
                            if (s1Seq == null || !s1Seq.next()) {
                                this.state = 4;
                                continue;
                            }
                            this.state = 2;
                            continue;
                        case 2:
                            //noinspection ConstantConditions
                            this.yield(s1Seq.getCurrent());
                            this.state = 3;
                            return;
                        case 3:
                            this.state = 1;
                            continue;
                        case 4:
                            s2Seq = engine.eval(s2, input);
                            this.state = 5;
                            continue;
                        case 5:
                            if (s2Seq == null || !s2Seq.next()) {
                                this.state = 8;
                                continue;
                            }
                            this.state = 6;
                            continue;
                        case 6:
                            //noinspection ConstantConditions
                            this.yield(s2Seq.getCurrent());
                            this.state = 7;
                            return;
                        case 7:
                            this.state = 5;
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
        return "or";
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
