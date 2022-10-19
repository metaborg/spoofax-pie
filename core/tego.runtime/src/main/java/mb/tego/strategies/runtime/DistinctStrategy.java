package mb.tego.strategies.runtime;

import mb.tego.sequences.Seq;
import mb.tego.sequences.SeqBase;
import mb.tego.strategies.NamedStrategy1;
import mb.tego.strategies.Strategy;
import mb.tego.utils.ExcludeFromJacocoGeneratedReport;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashSet;

/**
 * Distinct strategy.
 * <p>
 * This strategy returns a lazy sequence that skips any elements it returned previously.
 *
 * @param <T> the type of input (contravariant)
 * @param <R> the type of output (covariant)
 */
public final class DistinctStrategy<T, R> extends NamedStrategy1<Strategy<T, Seq<R>>, T, @Nullable Seq<R>> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final DistinctStrategy instance = new DistinctStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static <T, R> DistinctStrategy<T, R> getInstance() { return (DistinctStrategy<T, R>)instance; }

    private DistinctStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    public static <T, R> @Nullable Seq<R> eval(TegoEngine engine, Strategy<T, Seq<R>> s, T input) {
        @Nullable final Seq<R> rs = engine.eval(s, input);
        if (rs == null) return null;
        return new SeqBase<R>() {
            // Implementation if `yield` and `yieldBreak` could actually suspend computation
            @SuppressWarnings("unused")
            @ExcludeFromJacocoGeneratedReport
            private void computeNextCoroutine() throws InterruptedException {
                // 0:
                final HashSet<R> yielded = new HashSet<>();
                // 1:
                while (rs.next()) {
                    // 2:
                    final R r = rs.getCurrent();
                    if(!yielded.contains(r)) {
                        // We have not previously yielded this element,
                        // so we're going to yield it and not yield it again
                        yielded.add(r);
                        this.yield(r);
                    }
                    // 3:
                }
                // 4:
                yieldBreak();
            }

            // STATE MACHINE
            private int state = 0;
            // LOCAL VARIABLES
            private final HashSet<R> yielded = new HashSet<>();

            @SuppressWarnings("DuplicateBranchesInSwitch") @Override
            protected void computeNext() throws InterruptedException {
                while (true) {
                    switch (state) {
                        case 0:
                            this.state = 1;
                            continue;
                        case 1:
                            if (!rs.next()) {
                                this.state = 4;
                                continue;
                            }
                            this.state = 2;
                            continue;
                        case 2:
                            final R r = rs.getCurrent();
                            if (!yielded.contains(r)){
                                // We have not previously yielded this element,
                                // so we're going to yield it and not yield it again
                                yielded.add(r);
                                this.yield(r);
                                this.state = 3;
                                return;
                            }
                            this.state = 3;
                            continue;
                        case 3:
                            this.state = 1;
                            continue;
                        case 4:
                            yieldBreak();
                            this.state = -1;
                            return;
                        default:
                            throw new IllegalStateException("Illegal state: " + state);
                    }
                }
            }

            @Override
            public void close() throws Exception {
                super.close();
                yielded.clear();
            }
        };
    }

    @Override
    public @Nullable Seq<R> evalInternal(TegoEngine engine, Strategy<T, Seq<R>> s, T input) {
        return eval(engine, s, input);
    }

    @Override
    public String getName() {
        return "distinct";
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches") @Override
    public String getParamName(int index) {
        switch (index) {
            case 0: return "s";
            default: return super.getParamName(index);
        }
    }
}
