package mb.statix.strategies.runtime;

import mb.statix.sequences.Seq;
import mb.statix.sequences.SeqBase;
import mb.statix.strategies.NamedStrategy3;
import mb.statix.strategies.Strategy;
import mb.statix.utils.ExcludeFromJacocoGeneratedReport;

/**
 * Guarded left choice strategy.
 *
 * @param <CTX> the type of context (invariant)
 * @param <T> the type of input (contravariant)
 * @param <U> the type of intermediate (invariant)
 * @param <R> the type of output (covariant)
 */
public final class GlcStrategy<CTX, T, U, R> extends NamedStrategy3<CTX, Strategy<CTX, T, U>, Strategy<CTX, U, R>, Strategy<CTX, T, R>, T, R> {

    @SuppressWarnings("rawtypes")
    private static final GlcStrategy instance = new GlcStrategy();
    @SuppressWarnings("unchecked")
    public static <CTX, T, U, R> GlcStrategy<CTX, T, U, R> getInstance() { return (GlcStrategy<CTX, T, U, R>)instance; }

    private GlcStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    @Override
    public Seq<R> eval(CTX ctx, Strategy<CTX, T, U> condition, Strategy<CTX, U, R> onSuccess, Strategy<CTX, T, R> onFailure, T input) {
        return new SeqBase<R>() {
            // Implementation if `yield` and `yieldBreak` could actually suspend computation
            @SuppressWarnings("unused")
            @ExcludeFromJacocoGeneratedReport
            private void computeNextCoroutine() throws InterruptedException {
                // 0:
                final Seq<U> conditionSeq = condition.eval(ctx, input);
                if (conditionSeq.next()) {
                    // 1:
                    do {
                        // 2:
                        final U current = conditionSeq.getCurrent();
                        final Seq<R> onSuccessSeq = onSuccess.eval(ctx, current);
                        // 3:
                        while(onSuccessSeq.next()) {
                            // 4:
                            this.yield(onSuccessSeq.getCurrent());
                            // 5:
                        }
                        // 6:
                    } while (conditionSeq.next());
                    // 7:
                } else {
                    // 8:
                    final Seq<R> onFailureSeq = onFailure.eval(ctx, input);
                    // 9:
                    while(onFailureSeq.next()) {
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
            private Seq<U> conditionSeq;
            private Seq<R> onSuccessSeq;
            private Seq<R> onFailureSeq;

            @Override
            protected void computeNext() throws InterruptedException {
                while (true) {
                    switch (state) {
                        case 0:
                            conditionSeq = condition.eval(ctx, input);
                            if (!conditionSeq.next()) {
                                this.state = 8;
                                continue;
                            }
                            this.state = 1;
                            continue;
                        case 1:
                            this.state = 2;
                            continue;
                        case 2:
                            final U current = conditionSeq.getCurrent();
                            onSuccessSeq = onSuccess.eval(ctx, current);
                            this.state = 3;
                            continue;
                        case 3:
                            if (!onSuccessSeq.next()) {
                                this.state = 6;
                                continue;
                            }
                            this.state = 4;
                            continue;
                        case 4:
                            this.yield(onSuccessSeq.getCurrent());
                            this.state = 5;
                            return;
                        case 5:
                            this.state = 3;
                            continue;
                        case 6:
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
                            onFailureSeq = onFailure.eval(ctx, input);
                            this.state = 9;
                            continue;
                        case 9:
                            if (!onFailureSeq.next()) {
                                this.state = 12;
                                continue;
                            }
                            this.state = 10;
                            continue;
                        case 10:
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
