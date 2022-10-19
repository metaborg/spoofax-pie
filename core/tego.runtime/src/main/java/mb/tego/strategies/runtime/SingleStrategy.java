package mb.tego.strategies.runtime;

import mb.tego.sequences.Seq;
import mb.tego.sequences.SeqBase;
import mb.tego.strategies.NamedStrategy1;
import mb.tego.strategies.Strategy;
import mb.tego.utils.ExcludeFromJacocoGeneratedReport;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * single(s) strategy.
 * <p>
 * This returns the results of the strategy if it returns exactly one result, otherwise the strategy fails.
 *
 * @param <T> the type of input (contravariant)
 * @param <R> the type of output (covariant)
 */
public final class SingleStrategy<T, R> extends NamedStrategy1<Strategy<T, Seq<R>>, T, Seq<R>> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final SingleStrategy instance = new SingleStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static <T, R> SingleStrategy<T, R> getInstance() { return (SingleStrategy<T, R>)instance; }

    private SingleStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    public static <T, R> Seq<R> eval(TegoEngine engine, Strategy<T, Seq<R>> s, T input) {
        return new SeqBase<R>() {
            // Implementation if `yield` and `yieldBreak` could actually suspend computation
            @SuppressWarnings("unused")
            @ExcludeFromJacocoGeneratedReport
            private void computeNextCoroutine() throws InterruptedException {
                // 0:
                final @Nullable Seq<R> sSeq = engine.eval(s, input);
                if (sSeq != null && sSeq.next()) {
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
                            final @Nullable Seq<R> sSeq = engine.eval(s, input);
                            if (sSeq != null && sSeq.next()) {
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
    public Seq<R> evalInternal(TegoEngine engine, Strategy<T, Seq<R>> s, T input) {
        return eval(engine, s, input);
    }

    @Override
    public String getName() {
        return "single";
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches") @Override
    public String getParamName(int index) {
        switch (index) {
            case 0: return "s";
            default: return super.getParamName(index);
        }
    }
}
