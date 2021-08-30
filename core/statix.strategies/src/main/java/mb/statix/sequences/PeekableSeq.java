package mb.statix.sequences;

/**
 * A sequence that allows peeking whether there is a next value.
 * @param <T> the type of elements in the sequence (covariant)
 */
public interface PeekableSeq<T> extends Seq<T> {
    /**
     * Peeks at the next value and returns whether it is present,
     * but does not move the iterator.
     *
     * @return {@code true} when a call to {@link #next} will put the iterator on a valid element;
     * otherwise, {@code false} when the end of the sequence has been reached
     * @throws InterruptedException if the operation was interrupted
     */
    boolean peek() throws InterruptedException;

    @Override default PeekableSeq<T> peekable() { return this; }
}
