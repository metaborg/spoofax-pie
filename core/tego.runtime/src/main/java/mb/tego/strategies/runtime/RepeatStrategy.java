package mb.tego.strategies.runtime;

import mb.tego.sequences.Seq;
import mb.tego.sequences.SeqBase;
import mb.tego.sequences.PeekableSeq;
import mb.tego.strategies.NamedStrategy1;
import mb.tego.strategies.Strategy;
import mb.tego.utils.ExcludeFromJacocoGeneratedReport;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayDeque;

/**
 * Repeat strategy.
 * <p>
 * This repeats applying the strategy, until the strategy fails.
 *
 * @param <T> the type of input and output (invariant)
 */
public final class RepeatStrategy<T> extends NamedStrategy1<Strategy<T, Seq<T>>, T, Seq<T>> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final RepeatStrategy instance = new RepeatStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static <T> RepeatStrategy<T> getInstance() { return (RepeatStrategy<T>)instance; }

    private RepeatStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    public static <T> Seq<T> eval(TegoEngine engine, Strategy<T, Seq<T>> s, T input) {
        return new SeqBase<T>() {
            // Implementation if `yield` and `yieldBreak` could actually suspend computation
            @SuppressWarnings("unused")
            @ExcludeFromJacocoGeneratedReport
            private void computeNextCoroutine() throws InterruptedException {
                // To avoid as many computations as possible,
                // this implementation maintains a stack of iterators.
                // Each time the strategy is evaluated, the resulting iterator
                // is pushed on the stack. As long as the iterator is not iterated,
                // no computations will be done.
                // 0:
                final ArrayDeque<Seq<T>> stack = new ArrayDeque<>();
                stack.push(Seq.of(input));
                // 1:
                while (!stack.isEmpty()) {
                    // 2:
                    // Get the next non-empty iterator on the stack
                    final Seq<T> seq = stack.peek();
                    if (!seq.next()) {
                        stack.pop();
                        continue;
                    }
                    final T element = seq.getCurrent();
                    final @Nullable Seq<T> resultSeq = engine.eval(s, element);
                    final @Nullable PeekableSeq<T> result = resultSeq != null ? resultSeq.peekable() : null;
                    if (result == null || !result.peek()) {
                        // The strategy failed. Yield the element itself.
                        this.yield(element);
                        // 3:
                    } else {
                        // The strategy succeeded. Push the resulting iterator on the stack.
                        stack.push(result);
                    }
                    // 4:
                }
                // 5:
                yieldBreak();
            }

            // STATE MACHINE
            private int state = 0;
            // LOCAL VARIABLES
            private final ArrayDeque<Seq<T>> stack = new ArrayDeque<>();

            @Override
            protected void computeNext() throws InterruptedException {
                while (true) {
                    switch (state) {
                        case 0:
                            stack.push(Seq.of(input));
                            this.state = 1;
                            continue;
                        case 1:
                            if (stack.isEmpty()) {
                                this.state = 5;
                                continue;
                            }
                            this.state = 2;
                            continue;
                        case 2:
                            // Get the next non-empty iterator on the stack
                            assert stack.peek() != null;
                            final Seq<T> seq = stack.peek();
                            if (!seq.next()) {
                                stack.pop();
                                this.state = 1;
                                continue;
                            }
                            final T element = seq.getCurrent();
                            final @Nullable Seq<T> resultSeq = engine.eval(s, element);
                            final @Nullable PeekableSeq<T> result = resultSeq != null ? resultSeq.peekable() : null;
                            if (result == null || !result.peek()) {
                                // The strategy failed. Yield the element itself.
                                this.yield(element);
                                this.state = 3;
                                return;
                            }
                            // The strategy succeeded. Push the resulting iterator on the stack.
                            stack.push(result);
                            this.state = 4;
                            continue;
                        case 3:
                            this.state = 4;
                            continue;
                        case 4:
                            this.state = 1;
                            continue;
                        case 5:
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
    public Seq<T> evalInternal(TegoEngine engine, Strategy<T, Seq<T>> s, T input) {
        return eval(engine, s, input);
    }

    @Override
    public String getName() {
        return "repeat";
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches") @Override
    public String getParamName(int index) {
        switch (index) {
            case 0: return "s";
            default: return super.getParamName(index);
        }
    }
}
