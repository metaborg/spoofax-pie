package mb.tego.strategies3.runtime;

import mb.tego.patterns.Pattern;
import mb.tego.sequences.PeekableSeq;
import mb.tego.sequences.Seq;
import mb.tego.sequences.SeqBase;
import mb.tego.strategies3.NamedStrategy1;
import mb.tego.utils.ExcludeFromJacocoGeneratedReport;

/**
 * Strategy that matches the input against a pattern.
 *
 * @param <T> the type of input (contravariant)
 * @param <R> the type of output (covariant)
 */
public final class MatchStrategy<T, R> extends NamedStrategy1<Pattern<T, R>, T, R> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final MatchStrategy instance = new MatchStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static <T, R> MatchStrategy<T, R> getInstance() { return (MatchStrategy<T, R>)instance; }

    private MatchStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    public static <T, R> Seq<R> eval(TegoEngine engine, Pattern<T, R> pattern, T input) {
        return new SeqBase<R>() {
            // Implementation if `yield` and `yieldBreak` could actually suspend computation
            @SuppressWarnings("unused")
            @ExcludeFromJacocoGeneratedReport
            private void computeNextCoroutine() {
                // 0:
                final boolean matches = pattern.match(input);
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
                            final boolean matches = pattern.match(input);
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

    @Override public Seq<R> evalInternal(TegoEngine engine, Pattern<T, R> pattern, T input) {
        return eval(engine, pattern, input);
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
