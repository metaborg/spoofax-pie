package mb.statix.sequences;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Collector;

/**
 * A lazy sequence is a lazy computation of multiple values.
 *
 * The values in a sequence can be {@code null}.
 * The sequence can only be iterated <i>once</i>.
 *
 * The iterator is initially positioned <i>before</i> the first element,
 * and will be positioned <i>after</i> the last element once all elements
 * have been iterated.
 *
 * The iterator should be closed when done, to release any resources it holds.
 *
 * @param <T> the type of values in the sequence (covariant)
 */
public interface Seq<T> extends AutoCloseable {

    /**
     * Gets the current element in the iterator.
     *
     * Note that the behavior is undefined when the iterator is not positioned
     * on a valid element. In this case, this method may return {@code null},
     * may return another value, may return an old value, or throw an exception.
     *
     * Initially the iterator is positioned <i>before</i> the first element.
     *
     * @return the current element
     */
    T getCurrent();

    /**
     * Moves to the next element in the iterator, if any.
     *
     * @return {@code true} when the iterator is now on a valid element;
     * otherwise, {@code false} when the end of the iterator has been reached
     * @throws InterruptedException if the operation was interrupted
     */
    boolean next() throws InterruptedException;

    /**
     * Returns an empty lazy sequence.
     *
     * @param <T> the type of values in the sequence (covariant)
     * @return the empty sequence
     */
    @SuppressWarnings("unchecked")
    static <T> Seq<T> of() {
        return EmptySeq.instance;
    }

    /**
     * Returns a lazy sequence with the specified elements.
     *
     * @param elements the elements in the sequence
     * @param <T> the type of values in the sequence (covariant)
     * @return the sequence
     */
    @SafeVarargs static <T> Seq<T> of(T... elements) {
        if (elements.length == 0) return of();
        return new ArraySeq<>(elements);
    }

    /**
     * Returns a lazy sequence that returns a (possibly infinite) sequence by calling the given supplier.
     *
     * The sequence returns elements until the supplier throws an exception.
     * The thrown exception is propagated to the caller.
     * If the supplier throws {@link NoSuchElementException},
     * then the sequence is finished but no exception is thrown.
     *
     * @param supplier the supplier
     * @param <T> the type of values being supplied (covariant)
     * @return the sequence
     */
    static <T> Seq<T> from(InterruptibleSupplier<T> supplier) {
        return new SuppliedSeq<>(supplier);
    }

    /**
     * Returns a lazy sequence of at most one element that results from calling the given supplier once.
     *
     * The sequence returns one element unless the supplier throws an exception.
     * The thrown exception is propagated to the caller.
     * If the supplier throws {@link NoSuchElementException},
     * then the sequence is finished but no exception is thrown.
     *
     * @param supplier the supplier
     * @param <T> the type of value being supplied (covariant)
     * @return the sequence of one element
     */
    static <T> Seq<T> fromOnce(InterruptibleSupplier<T> supplier) {
        return new SuppliedSeq<>(supplier).limit(1);
    }

    /**
     * Turns an iterator into a lazy sequence.
     *
     * @param iterator the iterator to wrap
     * @param <T> the type of values in the iterator
     * @return the lazy sequence
     */
    static <T> Seq<T> asSeq(Iterator<T> iterator) {
        if (iterator instanceof SeqIterator) {
            // Return originally wrapped lazy sequence
            return ((SeqIterator<T>)iterator).seq;
        } else {
            // Wrap normal iterator
            return new IteratorSeq<>(iterator);
        }
    }

    /**
     * Turns a lazy sequence into an iterator.
     *
     * Note that any {@link InterruptedException} thrown in the unwrapped iterator
     * cause the thread's {@link Thread#isInterrupted()} to be set,
     * and throw a {@link RuntimeException}.
     *
     * @param seq the sequence to unwrap
     * @param <T> the type of values in the sequence
     * @return the iterator
     */
    static <T> Iterator<T> asIterator(Seq<T> seq) {
        if (seq instanceof IteratorSeq) {
            // Return originally wrapped iterator
            return ((IteratorSeq<T>)seq).iterator;
        } else {
            // Unwrap interruptible iterator
            return new SeqIterator<>(seq);
        }
    }

    /**
     * Collects the remaining elements of the sequence.
     *
     * @param collector the collector
     * @param <R> the type of results
     * @param <A> the type of collector
     * @return the result of the collector
     */
    default <R, A> R collect(Collector<? super T, A, R> collector) throws InterruptedException {
        final A container = collector.supplier().get();
        while (this.next()) {
            collector.accumulator().accept(container, this.getCurrent());
        }
        return collector.finisher().apply(container);
    }

    /**
     * Returns up to a specified number of elements from this sequence.
     *
     * Note that if this sequence has been (partially) iterated,
     * the resulting sequence starts at that point in this sequence.
     * If this sequence is being iterated in between calls to the resulting sequence,
     * the results are undefined.
     */
    default Seq<T> limit(int n) {
        return new LimitSeq<>(this, n);
    }

    /**
     * Takes the last element from this sequence,
     * or throws an exception if there is none or if it is not the last element.
     *
     * This is a terminal operation.
     *
     * @return the last element
     */
    default T single() throws InterruptedException {
        if (!this.next()) throw new NoSuchElementException("No more elements in the sequence.");
        final T current = this.getCurrent();
        if (this.next()) throw new IllegalStateException("More than one element in the sequence.");
        return current;
    }

    /**
     * Allows peeking to see if there is a next element.
     *
     * @return the peekable sequence
     */
    default PeekableSeq<T> peekable() {
        return new PeekingSeq<>(this);
    }

}

/**
 * An empty sequence.
 *
 * @param <T> the type of values in the sequence (covariant)
 */
final class EmptySeq<T> implements Seq<T> {
    @SuppressWarnings("rawtypes")
    public static EmptySeq instance = new EmptySeq();

    private EmptySeq() { }

    @Override
    public T getCurrent() {
        throw new NoSuchElementException("Positioned before or after the sequence.");
    }

    @Override
    public boolean next() throws InterruptedException {
        return false;
    }

    @Override
    public void close() {
        // Nothing to do.
    }
}

/**
 * Wraps an array.
 *
 * @param <T> the type of elements in the array (covariant)
 */
final class ArraySeq<T> implements Seq<T> {
    private int index = -1;
    private final T[] elements;

    public ArraySeq(T[] elements) {
        this.elements = elements;
    }

    @Override
    public T getCurrent() {
        if (index < 0 || index >= elements.length)
            throw new NoSuchElementException("Positioned before or after the sequence.");
        return elements[index];
    }

    @Override
    public boolean next() throws InterruptedException {
        index += 1;
        return index >= 0 && index < elements.length;
    }

    @Override
    public void close() {
        // Nothing to do.
    }
}

/**
 * Wraps a supplier.
 *
 * @param <T> the type of elements in the array (covariant)
 */
final class SuppliedSeq<T> extends SeqBase<T> {
    private final InterruptibleSupplier<T> supplier;

    public SuppliedSeq(InterruptibleSupplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    protected void computeNext() throws InterruptedException {
        try {
            final T value = this.supplier.get();
            this.yield(value);
        } catch (NoSuchElementException ex) {
            yieldBreak();
        } catch (Throwable ex) {
            yieldBreak();
            throw ex;
        }
    }

    @Override
    public void close() throws Exception {
        if (this.supplier instanceof AutoCloseable) {
            ((AutoCloseable)this.supplier).close();
        }
    }
}

/**
 * Returns up to a specified number of elements from the given sequence.
 *
 * Note that if the given sequence has been (partially) iterated,
 * this sequence starts at that point in the given sequence.
 * If the given sequence is being iterated in between calls to this sequence,
 * the results are undefined.
 *
 * @param <T> the type of elements in the array (covariant)
 */
final class LimitSeq<T> extends SeqBase<T> {
    private final Seq<T> seq;
    private int limit;

    public LimitSeq(Seq<T> seq, int limit) {
        this.seq = seq;
        this.limit = limit;
    }

    @Override
    protected void computeNext() throws InterruptedException {
        if (limit <= 0) {
            yieldBreak();
            return;
        }
        limit -= 1;
        seq.next();
        this.yield(seq.getCurrent());
    }

    @Override
    public void close() throws Exception {
        seq.close();
    }
}

/**
 * A sequence wrapper that allows peeking whether there is a next value.
 * @param <T> the type of elements in the sequence (covariant)
 */
final class PeekingSeq<T> implements PeekableSeq<T> {
    private final Seq<T> seq;
    private T current;
    private boolean hasNext;
    private boolean peeked = false;

    public PeekingSeq(Seq<T> seq) {
        this.seq = seq;
    }

    @Override
    public T getCurrent() {
        return this.peeked ? this.current : this.seq.getCurrent();
    }

    @Override
    public boolean next() throws InterruptedException {
        // Did we peek?
        if (!this.peeked) {
            return this.seq.next();
        } else {
            this.peeked = false;
            return this.hasNext;
        }
    }

    /**
     * Peeks at the next value and returns whether it is present,
     * but does not move the iterator.
     *
     * @return {@code true} when a call to {@link #next} will put the iterator on a valid element;
     * otherwise, {@code false} when the end of the sequence has been reached
     * @throws InterruptedException if the operation was interrupted
     */
    public boolean peek() throws InterruptedException {
        // Have we peeked yet?
        if (!this.peeked) {
            // Store the current value
            this.current = this.seq.getCurrent();
            // Compute the next value
            this.hasNext = this.seq.next();
            // Note that the inner sequence is ahead.
            this.peeked = true;
        }
        return this.hasNext;
    }

    @Override
    public void close() throws Exception {
        seq.close();
    }
}

/**
 * Sequence wrapping an {@link Iterator}.
 *
 * @param <T> the type of values in the iterator (covariant)
 */
final class IteratorSeq<T> extends SeqBase<T> {
    final Iterator<T> iterator;

    public IteratorSeq(Iterator<T> iterator) {
        this.iterator = iterator;
    }

    @Override
    protected void computeNext() throws InterruptedException {
        if (iterator.hasNext()) {
            this.yield(iterator.next());
        } else {
            yieldBreak();
        }
    }
}

/**
 * Iterator wrapping a {@link Seq}.
 *
 * Note that any {@link InterruptedException} thrown in the unwrapped sequence
 * cause the thread's {@link Thread#isInterrupted()} to be set,
 * and throw a {@link RuntimeException}.
 *
 * The iterator should be closed when done, to release any resources it holds.
 *
 * @param <T> the type of values in the sequence (covariant)
 */
class SeqIterator<T> implements Iterator<T>, AutoCloseable {
    final Seq<T> seq;
    private boolean consumed = false;

    public SeqIterator(Seq<T> seq) {
        this.seq = seq;
    }

    @Override
    public boolean hasNext() {
        try {
            this.consumed = false;
            return seq.next();
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Operation interrupted.", ex);
        }
    }

    @Override
    public T next() {
        if (this.consumed) {
            // THROWS: RuntimeException
            final boolean produced = hasNext();
            if (!produced) throw new NoSuchElementException("Sequence is finished.");
        }
        this.consumed = true;
        return seq.getCurrent();
    }

    @Override
    public void close() throws Exception {
        this.seq.close();
    }
}
