package mb.statix.sequences;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Base class for lazy sequences.
 *
 * @param <T> the type of values in the sequence (covariant)
 */
public abstract class SeqBase<T> implements Seq<T> {

    /**
     * Specifies the state of the iterator.
     */
    private enum State {
        /** The iterator has not yet computed the next element. */
        Preparing,
        /** The iterator has computed the next element. */
        Ready,
        /** The iterator has no more elements. */
        Finished,
    }

    private State state = State.Preparing;

    @Nullable private T current = null;

    @SuppressWarnings({"ConstantConditions", "NullableProblems"})
    @Override public final T getCurrent() {
        return this.current;
    }

    @Override
    public final boolean next() throws InterruptedException {
        if (this.state == State.Finished) return false;
        this.state = State.Preparing;
        computeNext();
        assert state != State.Preparing : "No call to either yield() or yieldBreak() was performed this iteration.";
        return this.state == State.Ready;
    }

    /**
     * Computes the next element for the sequence.
     *
     * This method should call either {@link #yield} to yield the next element,
     * or {@link #yieldBreak} to indicate the end of the sequence.
     */
    protected abstract void computeNext() throws InterruptedException;

    /**
     * Indicates what the next element will be.
     *
     * Only one element can be the next element.
     * The caller must return from the method.
     *
     * NOTE: To call this function, you need to prefix it with {@code this}, thus {@code this.yield()}.
     * Unfortunately, calling just {@code yield()} is not allowed.
     *
     * @param value the next element
     */
    protected final void yield(T value) {
        assert state == State.Preparing : "Only one call to either yield() or yieldBreak() is allowed per iteration.";
        this.current = value;
        this.state = State.Ready;
    }

    /**
     * Indicates that the iterator is done.
     *
     * The iterator can only finish once.
     * The caller must return from the method.
     */
    protected final void yieldBreak() {
        assert state == State.Preparing : "Only one call to either yield() or yieldBreak() is allowed per iteration.";
        // Set to null to release any object from this iterator for garbage collection.
        this.current = null;
        this.state = State.Finished;
    }

    /**
     * Override this method to perform any closing operations.
     * @throws Exception if an exception occurs
     */
    @Override
    public void close() throws Exception {
        // Nothing to do.
    }
}
