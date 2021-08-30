package mb.statix.strategies.runtime;

import mb.statix.sequences.Seq;
import mb.statix.sequences.SeqBase;
import mb.statix.sequences.PeekableSeq;
import mb.statix.strategies.NamedStrategy1;
import mb.statix.strategies.Strategy;
import mb.statix.utils.ExcludeFromJacocoGeneratedReport;

import java.util.ArrayDeque;

/**
 * Repeat strategy.
 *
 * This repeats applying the strategy, until the strategy fails.
 *
 * @param <CTX> the type of context (invariant)
 * @param <T> the type of input and output (invariant)
 */
public final class RepeatStrategy<CTX, T> extends NamedStrategy1<CTX, Strategy<CTX, T, T>, T, T> {

    @SuppressWarnings("rawtypes")
    private static final RepeatStrategy instance = new RepeatStrategy();
    @SuppressWarnings("unchecked")
    public static <CTX, T> RepeatStrategy<CTX, T> getInstance() { return (RepeatStrategy<CTX, T>)instance; }

    private RepeatStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    @Override
    public Seq<T> eval(CTX ctx, Strategy<CTX, T, T> s, T input) {
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
                    final PeekableSeq<T> result = s.eval(ctx, element).peekable();
                    if (!result.peek()) {
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
                            final PeekableSeq<T> result = s.eval(ctx, element).peekable();
                            if (!result.peek()) {
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
    public String getName() {
        return "repeat";
    }

    @Override
    public String getParamName(int index) {
        switch (index) {
            case 0: return "s";
            default: return super.getParamName(index);
        }
    }
}
