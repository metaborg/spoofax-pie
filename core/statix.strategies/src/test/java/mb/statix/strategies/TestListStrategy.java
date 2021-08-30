package mb.statix.strategies;

import mb.statix.sequences.Seq;
import mb.statix.sequences.SeqBase;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * A test strategy that returns the elements from a list,
 * and counts the number of calls to {@code #eval()}, {@code Seq#iterator()},
 * and {@code InterruptibleIteratorBase#computeNext()}.
 *
 * @param <T> the type of input
 * @param <R> the type of output
 */
public final class TestListStrategy<T, R> implements Strategy<Object, T, R> {
    public final AtomicInteger evalCalls = new AtomicInteger();
    public final AtomicInteger nextCalls = new AtomicInteger();
    private final Function<T, List<R>> transformation;

    public TestListStrategy(Function<T, List<R>> transformation) {
        this.transformation = transformation;
    }

    @Override
    public Seq<R> eval(Object o, T input) {
        evalCalls.incrementAndGet();
        final List<R> results = transformation.apply(input);
        return new SeqBase<R>() {
            private int index = 0;
            @Override
            protected void computeNext() throws InterruptedException {
                nextCalls.incrementAndGet();
                if (index < 0 || index >= results.size()) {
                    yieldBreak();
                } else {
                    this.yield(results.get(index));
                    index += 1;
                }
            }
        };
    }
};
