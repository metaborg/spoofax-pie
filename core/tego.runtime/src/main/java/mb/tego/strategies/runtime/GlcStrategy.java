package mb.tego.strategies.runtime;

import mb.tego.sequences.Seq;
import mb.tego.sequences.SeqBase;
import mb.tego.strategies.NamedStrategy3;
import mb.tego.strategies.Strategy;
import mb.tego.utils.ExcludeFromJacocoGeneratedReport;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Guarded left choice strategy.
 *
 * @param <T> the type of input (contravariant)
 * @param <U> the type of intermediate (invariant)
 * @param <R> the type of output (covariant)
 */
public final class GlcStrategy<T, U, R> extends NamedStrategy3<Strategy<T, Seq<U>>, Strategy<U, Seq<R>>, Strategy<T, Seq<R>>, T, Seq<R>> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final GlcStrategy instance = new GlcStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static <T, U, R> GlcStrategy<T, U, R> getInstance() { return (GlcStrategy<T, U, R>)instance; }

    private GlcStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    public static <T, U, R> Seq<R> eval(TegoEngine engine, Strategy<T, Seq<U>> condition, Strategy<U, Seq<R>> onSuccess, Strategy<T, Seq<R>> onFailure, T input) {
        return new SeqBase<R>() {
            // Implementation if `yield` and `yieldBreak` could actually suspend computation
            @SuppressWarnings("unused")
            @ExcludeFromJacocoGeneratedReport
            private void computeNextCoroutine() throws InterruptedException {
                // 0:
                final @Nullable Seq<U> conditionSeq = engine.eval(condition, input);
                if (conditionSeq != null && conditionSeq.next()) {
                    // 1:
                    do {
                        // 2:
                        final U current = conditionSeq.getCurrent();
                        final @Nullable Seq<R> onSuccessSeq = engine.eval(onSuccess, current);
                        // 3:
                        while(onSuccessSeq != null && onSuccessSeq.next()) {
                            // 4:
                            this.yield(onSuccessSeq.getCurrent());
                            // 5:
                        }
                        // 6:
                    } while (conditionSeq.next());
                    // 7:
                } else {
                    // 8:
                    final @Nullable Seq<R> onFailureSeq = engine.eval(onFailure, input);
                    // 9:
                    while(onFailureSeq != null && onFailureSeq.next()) {
                        // 10:
                        this.yield(onFailureSeq.getCurrent());
                        // 11:
                    }
                    // 12:
                }
                // 13:
                yieldBreak();
            }

            // STATE MACHINE
            private int state = 0;
            // LOCAL VARIABLES
            private @Nullable Seq<U> conditionSeq;
            private @Nullable Seq<R> onSuccessSeq;
            private @Nullable Seq<R> onFailureSeq;

            @SuppressWarnings("DuplicateBranchesInSwitch") @Override
            protected void computeNext() throws InterruptedException {
                while (true) {
                    switch (state) {
                        case 0:
                            conditionSeq = engine.eval(condition, input);
                            if (conditionSeq == null || !conditionSeq.next()) {
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
                            final U current = conditionSeq.getCurrent();
                            onSuccessSeq = engine.eval(onSuccess, current);
                            this.state = 3;
                            continue;
                        case 3:
                            if (onSuccessSeq == null || !onSuccessSeq.next()) {
                                this.state = 6;
                                continue;
                            }
                            this.state = 4;
                            continue;
                        case 4:
                            //noinspection ConstantConditions
                            this.yield(onSuccessSeq.getCurrent());
                            this.state = 5;
                            return;
                        case 5:
                            this.state = 3;
                            continue;
                        case 6:
                            //noinspection ConstantConditions
                            if (conditionSeq.next()) {
                                this.state = 1;
                                continue;
                            }
                            this.state = 7;
                            continue;
                        case 7:
                            this.state = 13;
                            continue;
                        case 8:
                            onFailureSeq = engine.eval(onFailure, input);
                            this.state = 9;
                            continue;
                        case 9:
                            if (onFailureSeq == null || !onFailureSeq.next()) {
                                this.state = 12;
                                continue;
                            }
                            this.state = 10;
                            continue;
                        case 10:
                            //noinspection ConstantConditions
                            this.yield(onFailureSeq.getCurrent());
                            this.state = 11;
                            return;
                        case 11:
                            this.state = 9;
                            continue;
                        case 12:
                            this.state = 13;
                            continue;
                        case 13:
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
    public Seq<R> evalInternal(TegoEngine engine, Strategy<T, Seq<U>> condition, Strategy<U, Seq<R>> onSuccess, Strategy<T, Seq<R>> onFailure, T input) {
        return eval(engine, condition, onSuccess, onFailure, input);
    }

    @Override
    public String getName() {
        return "glc";
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
