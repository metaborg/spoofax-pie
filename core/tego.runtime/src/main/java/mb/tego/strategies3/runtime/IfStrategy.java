package mb.tego.strategies3.runtime;

import mb.tego.sequences.Seq;
import mb.tego.sequences.SeqBase;
import mb.tego.strategies3.NamedStrategy3;
import mb.tego.strategies3.Strategy;
import mb.tego.utils.ExcludeFromJacocoGeneratedReport;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * If the conditional strategy succeeds, the success strategy is evaluated, otherwise the failure strategy is evaluated.
 *
 * @param <T> the type of input (contravariant)
 * @param <U> the type of intermediate (invariant)
 * @param <R> the type of output (covariant)
 */
public final class IfStrategy<T, U, R> extends NamedStrategy3<Strategy<T, U>, Strategy<T, R>, Strategy<T, R>, T, R> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final IfStrategy instance = new IfStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static <T, U, R> IfStrategy<T, U, R> getInstance() { return (IfStrategy<T, U, R>)instance; }

    private IfStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    public static <T, U, R> Seq<R> eval(
        TegoEngine engine,
        Strategy<T, U> condition,
        Strategy<T, R> onSuccess,
        Strategy<T, R> onFailure,
        T input
    ) {
        final Seq<U> conditionSeq = engine.eval(condition, input);
        final Seq<R> onSuccessSeq = engine.eval(onSuccess, input);
        final Seq<R> onFailureSeq = engine.eval(onFailure, input);
        return new SeqBase<R>() {
            // Implementation if `yield` and `yieldBreak` could actually suspend computation
            @SuppressWarnings("unused")
            @ExcludeFromJacocoGeneratedReport
            private void computeNextCoroutine() throws InterruptedException {
                // 0:
                if (conditionSeq.next()) {
                    // 1:
                    while(onSuccessSeq.next()) {
                        // 2:
                        this.yield(onSuccessSeq.getCurrent());
                        // 3:
                    }
                    // 4:
                } else {
                    // 5:
                    while(onFailureSeq.next()) {
                        // 6:
                        this.yield(onFailureSeq.getCurrent());
                        // 7:
                    }
                    // 8:
                }
                // 9:
                yieldBreak();
            }

            // STATE MACHINE
            private int state = 0;


            @SuppressWarnings("DuplicateBranchesInSwitch") @Override
            protected void computeNext() throws InterruptedException {
                while (true) {
                    switch (state) {
                        case 0:
                            if (!conditionSeq.next()) {
                                this.state = 5;
                                continue;
                            }
                            this.state = 1;
                            continue;
                        case 1:
                            if (!onSuccessSeq.next()) {
                                this.state = 2;
                                continue;
                            }
                            this.state = 4;
                            continue;
                        case 2:
                            //noinspection ConstantConditions
                            this.yield(onSuccessSeq.getCurrent());
                            this.state = 3;
                            return;
                        case 3:
                            this.state = 1;
                            continue;
                        case 4:
                            this.state = 9;
                            continue;
                        case 5:
                            if (!onFailureSeq.next()) {
                                this.state = 8;
                                continue;
                            }
                            this.state = 6;
                            continue;
                        case 6:
                            //noinspection ConstantConditions
                            this.yield(onFailureSeq.getCurrent());
                            this.state = 7;
                            return;
                        case 7:
                            this.state = 5;
                            continue;
                        case 8:
                            this.state = 9;
                            continue;
                        case 9:
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
    public Seq<R> evalInternal(
        TegoEngine engine,
        Strategy<T, U> condition,
        Strategy<T, R> onSuccess,
        Strategy<T, R> onFailure,
        T input
    ) {
        return eval(engine, condition, onSuccess, onFailure, input);
    }

    @Override
    public String getName() {
        return "if";
    }

    @Override
    public String getParamName(int index) {
        switch (index) {
            case 0: return "condition";
            case 1: return "onSuccess";
            case 2: return "onFailure";
            default: return super.getParamName(index);
        }
    }
}
