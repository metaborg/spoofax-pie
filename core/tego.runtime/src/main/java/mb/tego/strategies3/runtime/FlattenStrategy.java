package mb.tego.strategies3.runtime;

import mb.tego.sequences.Seq;
import mb.tego.sequences.SeqBase;
import mb.tego.strategies3.NamedStrategy;
import mb.tego.strategies3.NamedStrategy1;
import mb.tego.strategies3.Strategy;
import mb.tego.utils.ExcludeFromJacocoGeneratedReport;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Flatten strategy.
 * <p>
 * This wraps a strategy that returns a sequence of sequences and flattens the resulting sequence.
 *
 * @param <T> the type of input (contravariant)
 * @param <R> the type of output (covariant)
 */
public final class FlattenStrategy<T, R> extends NamedStrategy1<Strategy<T, Seq<R>>, T, R> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final FlattenStrategy instance = new FlattenStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static <T, R> FlattenStrategy<T, R> getInstance() { return (FlattenStrategy<T, R>)instance; }

    private FlattenStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    public static <T, R> Seq<R> eval(TegoEngine engine, Strategy<T, Seq<R>> s, T input) {
        final Seq<Seq<R>> rss = engine.eval(s, input);
        return new SeqBase<R>() {

            // Implementation if `yield` and `yieldBreak` could actually suspend computation
            @SuppressWarnings("unused")
            @ExcludeFromJacocoGeneratedReport
            private void computeNextCoroutine() throws InterruptedException {
                // 0:
                // 1:
                // 2:
                while (rss != null && rss.next()) {
                    // 3:
                    final Seq<R> rs = rss.getCurrent();
                    // 4:
                    while (rs != null && rs.next()) {
                        // 5:
                        final R r = rs.getCurrent();
                        this.yield(r);
                        // 6:
                    }
                    // 7:
                }
                // 8:
                yieldBreak();
            }

            // STATE MACHINE
            private int state = 0;
            // LOCAL VARIABLES
            private Seq<R> rs;

            @Override
            protected void computeNext() throws InterruptedException {
                while (true) {
                    switch (state) {
                        case 0:
                            this.state = 1;
                            continue;
                        case 1:
                            this.state = 2;
                            continue;
                        case 2:
                            if (rss == null || !rss.next()) {
                                this.state = 8;
                                continue;
                            }
                            this.state = 3;
                            continue;
                        case 3:
                            rs = rss.getCurrent();
                            this.state = 4;
                            continue;
                        case 4:
                            if (rs == null || !rs.next()) {
                                this.state = 7;
                                continue;
                            }
                            this.state = 5;
                            continue;
                        case 5:
                            //noinspection ConstantConditions
                            final R r = rs.getCurrent();
                            this.yield(r);
                            this.state = 6;
                            return;
                        case 6:
                            this.state = 4;
                            continue;
                        case 7:
                            this.state = 2;
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
    public Seq<R> evalInternal(TegoEngine engine, Strategy<T, Seq<R>> s, T input) {
        return eval(engine, s, input);
    }

    @Override
    public String getName() {
        return "flatten";
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches") @Override
    public String getParamName(int index) {
        switch (index) {
            case 0: return "s";
            default: return super.getParamName(index);
        }
    }

}
