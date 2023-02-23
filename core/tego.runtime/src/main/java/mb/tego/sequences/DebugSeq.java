package mb.tego.sequences;

/**
 * Logs when the wrapped sequence is iterated.
 *
 * @param <T> the type of elements in the sequence.
 */
public abstract class DebugSeq<T> extends SeqBase<T> {

    private int index = -1;
    private final Seq<T> innerSeq;

    /**
     * Initializes a new instance of the {@link DebugSeq} class.
     *
     * @param innerSeq the inner sequence
     */
    public DebugSeq(Seq<T> innerSeq) {
        this.innerSeq = innerSeq;
    }

    @Override
    protected void computeNext() throws InterruptedException {
        index += 1;
        onBeforeNext(index);
        final boolean success = innerSeq.next();
        if (!success) {
            onEnd(index);
            yieldBreak();
        } else {
            final T result = innerSeq.getCurrent();
            this.yield(onAfterNext(index, result));
        }
    }

    /**
     * Called before the next computation.
     *
     * @param index the zero-based index of the computation in this sequence
     */
    protected abstract void onBeforeNext(int index);

    /**
     * Called after the next computation.
     *
     * @param index the zero-based index of the computation in this sequence
     * @param result the result of the computation
     * @return the (possibly modified) result
     */
    protected abstract T onAfterNext(int index, T result);

    /**
     * Called when the computation yielded no more results
     *
     * @param index the zero-based index of the computation that failed
     */
    protected abstract void onEnd(int index);
}
