package mb.statix.strategies.runtime;

import mb.statix.patterns.Pattern;
import mb.statix.sequences.PeekableSeq;
import mb.statix.sequences.Seq;
import mb.statix.sequences.SeqBase;
import mb.statix.strategies.NamedStrategy1;
import mb.statix.utils.ExcludeFromJacocoGeneratedReport;

import java.util.ArrayDeque;
import java.util.HashSet;

/**
 * Strategy that matches the input against a pattern.
 *
 * @param <CTX> the type of context (invariant)
 * @param <T> the type of input (contravariant)
 * @param <R> the type of output (covariant)
 */
public final class MatchStrategy<CTX, T, R> extends NamedStrategy1<CTX, Pattern<CTX, T, R>, T, Seq<R>> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final MatchStrategy instance = new MatchStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static <CTX, T, R> MatchStrategy<CTX, T, R> getInstance() { return (MatchStrategy<CTX, T, R>)instance; }

    private MatchStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    public static <CTX, T, R> Seq<R> eval(TegoEngine engine, CTX ctx, Pattern<CTX, T, R> pattern, T input) {
        return new SeqBase<R>() {
            // Implementation if `yield` and `yieldBreak` could actually suspend computation
            @SuppressWarnings("unused")
            @ExcludeFromJacocoGeneratedReport
            private void computeNextCoroutine() {
                // 0:
                final boolean matches = pattern.match(ctx, input);
                if (matches) {
                    //noinspection unchecked
                    this.yield((R)input);
                }
                // 1:
                yieldBreak();
            }

            // STATE MACHINE
            private int state = 0;
            // LOCAL VARIABLES
            // none

            @Override
            protected void computeNext() {
                while (true) {
                    switch(state) {
                        case 0:
                            final boolean matches = pattern.match(ctx, input);
                            if (matches) {
                                //noinspection unchecked
                                this.yield((R)input);
                                this.state = 1;
                                return;
                            }
                            this.state = 1;
                            break;
                        case 1:
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

    @Override public Seq<R> evalInternal(TegoEngine engine, CTX ctx, Pattern<CTX, T, R> pattern, T input) {
        return eval(engine, ctx, pattern, input);
    }

    @Override
    public String getName() {
        return "match";
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches") @Override
    public String getParamName(int index) {
        switch (index) {
            case 0: return "pattern";
            default: return super.getParamName(index);
        }
    }

}
