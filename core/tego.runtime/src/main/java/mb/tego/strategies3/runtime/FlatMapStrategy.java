package mb.tego.strategies3.runtime;

import mb.tego.sequences.Seq;
import mb.tego.sequences.SeqBase;
import mb.tego.strategies3.NamedStrategy2;
import mb.tego.strategies3.Strategy1;
import mb.tego.utils.ExcludeFromJacocoGeneratedReport;

/**
 * ForEach strategy.
 * <p>
 * This takes a sequence argument and runs the given strategy with each element from the sequence on the input,
 * returning the sequence of results.
 *
 * @param <T> the type of input (contravariant)
 * @param <R> the type of output (covariant)
 */
public final class FlatMapStrategy<A, T, R> extends NamedStrategy2<Seq<A>, Strategy1<A, T, R>, T, R> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final FlatMapStrategy instance = new FlatMapStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static <A, T, R> FlatMapStrategy<A, T, R> getInstance() { return (FlatMapStrategy<A, T, R>)instance; }

    private FlatMapStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    public static <A, T, U, R> Seq<R> eval(TegoEngine engine, Seq<A> seq, Strategy1<A, T, R> s, T input) {
        return new SeqBase<R>() {

            // Implementation if `yield` and `yieldBreak` could actually suspend computation
            @SuppressWarnings("unused")
            @ExcludeFromJacocoGeneratedReport
            private void computeNextCoroutine() throws InterruptedException {
                // 0:
                // 1:
                while (seq.next()) {
                    // 2:
                    final A a = seq.getCurrent();
                    final Seq<R> r = engine.eval(s, a, input);
                    // 3:
                    while (r.next()) {
                        // 4:
                        this.yield(r.getCurrent());
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
            private Seq<R> r;

            @Override
            protected void computeNext() throws InterruptedException {
                while (true) {
                    switch (state) {
                        case 0:
                            this.state = 1;
                            continue;
                        case 1:
                            if (!seq.next()) {
                                this.state = 7;
                                continue;
                            }
                            this.state = 2;
                            continue;
                        case 2:
                            final A a = seq.getCurrent();
                            r = engine.eval(s, a, input);
                            this.state = 3;
                            continue;
                        case 3:
                            if (!r.next()) {
                                this.state = 6;
                                continue;
                            }
                            this.state = 4;
                            continue;
                        case 4:
                            this.yield(r.getCurrent());
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
    public Seq<R> evalInternal(TegoEngine engine, Seq<A> seq, Strategy1<A, T, R> s, T input) {
        return eval(engine, seq, s, input);
    }

    @Override
    public String getName() {
        return "flatMap";
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches") @Override
    public String getParamName(int index) {
        switch (index) {
            case 0: return "seq";
            case 1: return "s";
            default: return super.getParamName(index);
        }
    }

}
