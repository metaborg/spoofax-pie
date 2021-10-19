package mb.tego.sequences;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.Collectors;

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
     * If an exception occurs, the iterator is not positioned at a valid element.
     *
     * @return {@code true} when the iterator is now on a valid element;
     * otherwise, {@code false} when the end of the iterator has been reached
     * @throws InterruptedException if the operation was interrupted
     */
    boolean next() throws InterruptedException;

    /**
     * Returns an empty lazy sequence.
     *
     * This is an initial operation.
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
     * This is an initial operation.
     *
     * @param elements the elements in the sequence
     * @param <T> the type of values in the sequence (covariant)
     * @return the sequence
     */
    @SafeVarargs static <T> Seq<T> of(T... elements) {
        Objects.requireNonNull(elements, "'elements' must not be null.");
        if (elements.length == 0) return of();

        return new ArraySeq<>(elements);
    }

    /**
     * Returns a lazy sequence that returns a sequence from an iterable.
     *
     * This is an initial operation.
     *
     * The returned sequence will not try to instantiate the iterable's iterator
     * until it is called for the first time. Note that while the sequence
     * is iterating, many collections do not allow modifications or the
     * sequence iterator will fail.
     *
     * To create a lazy sequence from the current state of the iterable,
     * call {@code asSeq(iterable.iterator())} instead.
     *
     * @param iterable the iterable
     * @param <T> the type of values being supplied (covariant)
     * @return the sequence
     */
    static <T> Seq<T> from(Iterable<T> iterable) {
        Objects.requireNonNull(iterable, "'iterable' must not be null.");

        return new IterableSeq<>(iterable);
    }

    /**
     * Returns a lazy sequence that returns a (possibly infinite) sequence by calling the given supplier.
     *
     * This is an initial operation.
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
        Objects.requireNonNull(supplier, "'supplier' must not be null.");

        return new SuppliedSeq<>(supplier);
    }

    /**
     * Returns a lazy sequence of at most one element that results from calling the given supplier once.
     *
     * This is an initial operation.
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
        Objects.requireNonNull(supplier, "'supplier' must not be null.");

        return new SuppliedSeq<>(supplier).limit(1);
    }

    /**
     * Turns an iterator into a lazy sequence.
     *
     * This is an intermediate operation.
     *
     * @param iterator the iterator to wrap
     * @param <T> the type of values in the iterator
     * @return the lazy sequence
     */
    static <T> Seq<T> asSeq(Iterator<T> iterator) {
        Objects.requireNonNull(iterator, "'iterator' must not be null.");

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
     * This is an intermediate operation.
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
        Objects.requireNonNull(seq, "'seq' must not be null.");

        if (seq instanceof IteratorSeq) {
            // Return originally wrapped iterator
            return ((IteratorSeq<T>)seq).iterator;
        } else {
            // Unwrap interruptible iterator
            return new SeqIterator<>(seq);
        }
    }

    /**
     * Collects the remaining elements of the sequence into a list.
     *
     * This is a terminal operation.
     *
     * Note that if this sequence has been (partially) iterated,
     * the resulting list starts at that point in this sequence.
     *
     * @return the list
     */
    default List<T> toList() throws InterruptedException {
        return this.collect(Collectors.toList());
    }

    /**
     * Collects the remaining elements of the sequence.
     *
     * This is a terminal operation.
     *
     * Note that if this sequence has been (partially) iterated,
     * the resulting sequence starts at that point in this sequence.
     * If this sequence is being iterated in between calls
     * to the resulting sequence, the results are undefined.
     *
     * @param collector the collector
     * @param <R> the type of results
     * @param <A> the type of collector
     * @return the result of the collector
     */
    default <R, A> R collect(Collector<? super T, A, R> collector) throws InterruptedException {
        Objects.requireNonNull(collector, "'collector' must not be null.");

        final A container = collector.supplier().get();
        while (this.next()) {
            collector.accumulator().accept(container, this.getCurrent());
        }
        return collector.finisher().apply(container);
    }

    /**
     * Returns a sequence that contains only those elements that match the given predicate.
     *
     * This is an intermediate operation.
     *
     * Note that if this sequence has been (partially) iterated,
     * the resulting sequence starts at that point in this sequence.
     * If this sequence is being iterated in between calls
     * to the resulting sequence, the results are undefined.
     *
     * @param predicate the predicate to test
     * @return the new sequence
     */
    default Seq<T> filter(InterruptiblePredicate<T> predicate) {
        Objects.requireNonNull(predicate, "'predicate' must not be null.");

        return new FilterSeq<>(this, predicate);
    }

    /**
     * Returns a sequence that contains only those elements that match the given type.
     *
     * This is an intermediate operation.
     *
     * Note that if this sequence has been (partially) iterated,
     * the resulting sequence starts at that point in this sequence.
     * If this sequence is being iterated in between calls
     * to the resulting sequence, the results are undefined.
     *
     * @param cls the expected class
     * @return the new sequence
     */
    default <R> Seq<R> filterIsInstance(Class<R> cls) {
        Objects.requireNonNull(cls, "'cls' must not be null.");

        return new FilterIsInstanceSeq<>(this, cls);
    }

    /**
     * Performs the given action on each element.
     *
     * This is a terminal operation.
     *
     * Note that if this sequence has been (partially) iterated,
     * the resulting sequence starts at that point in this sequence.
     * If this sequence is being iterated in between calls
     * to the resulting sequence, the results are undefined.
     *
     * @param action the action to perform
     */
    default void forEach(Consumer<T> action) throws InterruptedException {
        Objects.requireNonNull(action, "'action' must not be null.");

        while (this.next()) {
            action.accept(this.getCurrent());
        }
    }

    /**
     * Returns a sequence with the results from applying a transform function to each of the original elements.
     *
     * This is an intermediate operation.
     *
     * Note that if this sequence has been (partially) iterated,
     * the resulting sequence starts at that point in this sequence.
     * If this sequence is being iterated in between calls
     * to the resulting sequence, the results are undefined.
     *
     * @param transform the transform function
     * @param <R> the type of results
     * @return the new sequence
     */
    default <R> Seq<R> map(InterruptibleFunction<T, R> transform) {
        Objects.requireNonNull(transform, "'transform' must not be null.");

        return new MapSeq<>(this, transform);
    }

    /**
     * Returns a sequence with the results from applying a transform function to each of the original elements,
     * returning only those elements that are not {@code null}.
     *
     * This is an intermediate operation.
     *
     * Note that if this sequence has been (partially) iterated,
     * the resulting sequence starts at that point in this sequence.
     * If this sequence is being iterated in between calls
     * to the resulting sequence, the results are undefined.
     *
     * @param transform the transform function, which may return {@code null}
     * @param <R> the type of results
     * @return the new sequence, without the {@code null} elements
     */
    default <R> Seq<R> mapNotNull(InterruptibleFunction<T, @Nullable R> transform) {
        Objects.requireNonNull(transform, "'transform' must not be null.");

        return new MapNotNullSeq<>(this, transform);
    }

    /**
     * Returns a sequence with the results from applying a transform function to each of the original elements,
     * returning only those elements that are present.
     *
     * This is an intermediate operation.
     *
     * Note that if this sequence has been (partially) iterated,
     * the resulting sequence starts at that point in this sequence.
     * If this sequence is being iterated in between calls
     * to the resulting sequence, the results are undefined.
     *
     * @param transform the transform function, which may return {@code null}
     * @param <R> the type of results
     * @return the new sequence, without the {@code null} elements
     */
    default <R> Seq<R> mapPresent(InterruptibleFunction<T, Optional<R>> transform) {
        Objects.requireNonNull(transform, "'transform' must not be null.");

        // NOTE: The documentation for Optional<T> states that the value should never be `null` if it is present.
        //  Therefore, we should be safe checking for null to determine whether the value is present.
        //noinspection ConstantConditions
        return this.mapNotNull(e -> transform.apply(e).orElse(null));
    }

    /**
     * Returns up to a specified number of elements from this sequence.
     *
     * This is an intermediate operation.
     *
     * Note that if this sequence has been (partially) iterated,
     * the resulting sequence starts at that point in this sequence.
     * If this sequence is being iterated in between calls
     * to the resulting sequence, the results are undefined.
     *
     * @param n the limit
     * @return the new sequence
     */
    default Seq<T> limit(int n) {
        if (n < 0) throw new IllegalArgumentException("'n' must be greater than or equal to 0.");

        return new LimitSeq<>(this, n);
    }

    /**
     * Takes the last element from this sequence,
     * or throws an exception if there is none or if it is not the last element.
     *
     * This is a terminal operation.
     *
     * Note that if this sequence has been (partially) iterated,
     * the resulting sequence starts at that point in this sequence.
     * If this sequence is being iterated in between calls
     * to the resulting sequence, the results are undefined.
     *
     * @return the last element
     * @throws NoSuchElementException if the sequence has zero elements
     * @throws IllegalStateException if the sequence has more than one element
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
     * This is an intermediate operation.
     *
     * @return the peekable sequence
     */
    default PeekableSeq<T> peekable() {
        return new BufferSeq<>(this, 1);
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
 * Filters a sequence.
 *
 * @param <T> the type of elements in the sequence (covariant)
 */
final class FilterSeq<T> extends SeqBase<T> {
    private final Seq<T> seq;
    private final InterruptiblePredicate<T> predicate;

    public FilterSeq(Seq<T> seq, InterruptiblePredicate<T> predicate) {
        this.seq = seq;
        this.predicate = predicate;
    }

    @Override
    protected void computeNext() throws InterruptedException {
        // Return elements that match the predicate
        while(seq.next()) {
            T value = seq.getCurrent();
            if(predicate.test(value)) {
                this.yield(value);
                return;
            }
        }

        // We're done
        yieldBreak();
    }

    @Override
    public void close() throws Exception {
        seq.close();
    }
}

/**
 * Filters a sequence based on the type of the elements.
 *
 * @param <T> the type of elements in the sequence (covariant)
 * @param <R> the type of elements in the result (contravariant)
 */
final class FilterIsInstanceSeq<T, R> extends SeqBase<R> {
    private final Seq<T> seq;
    private final Class<R> cls;

    public FilterIsInstanceSeq(Seq<T> seq, Class<R> cls) {
        this.seq = seq;
        this.cls = cls;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void computeNext() throws InterruptedException {
        // Return elements that match the predicate
        while(seq.next()) {
            T value = seq.getCurrent();
            if (cls.isAssignableFrom(value.getClass())) {
                this.yield((R)value);
                return;
            }
        }

        // We're done
        yieldBreak();
    }

    @Override
    public void close() throws Exception {
        seq.close();
    }
}

/**
 * Maps a sequence.
 *
 * @param <T> the type of elements in the sequence (covariant)
 * @param <R> the type of elements in the result (contravariant)
 */
final class MapSeq<T, R> extends SeqBase<R> {
    private final Seq<T> seq;
    private final InterruptibleFunction<T, R> transform;

    public MapSeq(
        Seq<T> seq,
        InterruptibleFunction<T, R> transform
    ) {
        this.seq = seq;
        this.transform = transform;
    }

    @Override
    protected void computeNext() throws InterruptedException {
        if (seq.next()) {
            T value = seq.getCurrent();
            R newValue = transform.apply(value);
            this.yield(newValue);
            return;
        }

        yieldBreak();
    }

    @Override
    public void close() throws Exception {
        seq.close();
    }
}

/**
 * Maps a sequence, filters to keep only the results that are not null.
 *
 * @param <T> the type of elements in the sequence (covariant)
 * @param <R> the type of elements in the result (contravariant)
 */
final class MapNotNullSeq<T, R> extends SeqBase<R> {
    private final Seq<T> seq;
    private final InterruptibleFunction<T, @Nullable R> transform;

    public MapNotNullSeq(
        Seq<T> seq,
        InterruptibleFunction<T, @Nullable R> transform
    ) {
        this.seq = seq;
        this.transform = transform;
    }

    @Override
    protected void computeNext() throws InterruptedException {
        while(seq.next()) {
            T value = seq.getCurrent();
            @Nullable R newValue = transform.apply(value);
            if(newValue != null) {
                this.yield(newValue);
                return;
            }
        }

        yieldBreak();
    }

    @Override
    public void close() throws Exception {
        seq.close();
    }
}

/**
 * Wraps a supplier.
 *
 * @param <T> the type of elements in the sequence (covariant)
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
 * @param <T> the type of elements in the sequence (covariant)
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
        final boolean hasNext = seq.next();
        if (hasNext) {
            this.yield(seq.getCurrent());
        } else {
            yieldBreak();
        }
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
 * Sequence wrapping an {@link Iterable}.
 *
 * @param <T> the type of values in the iterable (covariant)
 */
final class IterableSeq<T> extends SeqBase<T> {
    final Iterable<T> iterable;
    @Nullable private Iterator<T> iterator = null;

    public IterableSeq(Iterable<T> iterable) {
        this.iterable = iterable;
    }

    @Override
    protected void computeNext() throws InterruptedException {
        if (iterator == null) {
            iterator = iterable.iterator();
        }
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


/**
 * Base class for lazy sequences that are buffered.
 *
 * @param <T> the type of values in the sequence (covariant)
 */
final class BufferSeq<T> implements Seq<T>, PeekableSeq<T> {

    /** The sequence whose elements are buffered. */
    private final Seq<T> seq;
    /** The maximum buffer size. */
    private final int maxBufferSize;
    /** The buffer. */
    private final List<T> buffer;
    /** The current element. */
    @Nullable private T current = null;
    /** Whether the sequence is finished. */
    private boolean finished = false;

    /**
     * Initializes a new instance of the {@link BufferSeq} class.
     *
     * Note that a maximum buffer size of 0
     * will cause only the current element to be buffered.
     *
     * @param seq the sequence being buffered
     * @param maxBufferSize the maximum buffer size; or -1 to impose no limit
     */
    public BufferSeq(Seq<T> seq, int maxBufferSize) {
        this.seq = seq;
        this.maxBufferSize = maxBufferSize;
        this.buffer = new ArrayList<>(maxBufferSize >= 0 ? maxBufferSize : 10);
    }

    @SuppressWarnings({"NullableProblems", "ConstantConditions"})
    @Override
    public T getCurrent() {
        return this.current;
    }

    @Override
    public final boolean next() throws InterruptedException {
        if (this.finished) return false;
        if (this.buffer.size() > 0) {
            // Get and remove the head of the buffer
            this.current = this.buffer.remove(0);
            return true;
        } else if (this.seq.next()) {
            // Get the element from the sequence
            this.current = this.seq.getCurrent();
            return true;
        } else {
            // We're done here
            this.finished = true;
            return false;
        }
    }

    @Override
    public boolean peek() throws InterruptedException {
        final int newBufferSize = tryFill(1);
        return newBufferSize >= 1;
    }

    /**
     * Attempts to fill the buffer to contain the specified number of elements.
     *
     * @param count the target number of elements in the buffer
     * @return the actual number of elements in the buffer
     */
    private int tryFill(int count) throws InterruptedException {
        assert count >= 0;

        while(!this.finished && this.buffer.size() < count && (maxBufferSize < 0 || this.buffer.size() < maxBufferSize)) {
            if(!seq.next()) {
                // We're done here
                this.finished = true;
                break;
            }
            this.buffer.add(seq.getCurrent());
        }
        return this.buffer.size();
    }

    /**
     * Override this method to perform any closing operations.
     * @throws Exception if an exception occurs
     */
    @Override
    public void close() throws Exception {
        this.seq.close();
    }
}
