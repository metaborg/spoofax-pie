package mb.tego.sequences;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

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
        /** The iterator has not yet computed the element. */
        Preparing,
        /** The iterator has computed the element. */
        Ready,
        /** The iterator has no more elements. */
        Finished,
    }

    private State state = State.Preparing;

    @Nullable private T current = null;

    @SuppressWarnings("ConstantConditions")
    @Override
    public final T getCurrent() {
        return this.current;
    }

    @Override
    public final boolean next() throws InterruptedException {
        if (this.state == State.Finished) return false;
        this.state = State.Preparing;
        try {
            computeNext();
        } catch (NoSuchElementException ex) {
            yieldBreak();
        } catch (Throwable ex) {
            // NOTE: This may be an InterruptedException
            onError();
            throw ex;
        }
        assert this.state != State.Preparing : "No call to either yield() or yieldBreak() was performed this iteration.";
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

    // NOTE: A yieldAll(Seq) or yieldAll(Iterable) cannot be added here,
    // because an implementation of computeNext() can only return a single element.

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
     * Indicates that the iterator failed.
     */
    private void onError() {
        // NOTE: We explicitly don't check for the state here,
        // since we don't know whether the error occurred before
        // or after a call to `yield()` or `yieldBreak()`.
        this.current = null;
        this.state = State.Finished;
    }
}
