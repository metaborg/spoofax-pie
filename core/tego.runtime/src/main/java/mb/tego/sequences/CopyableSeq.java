package mb.tego.sequences;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A copyable sequence.
 * <p>
 * This buffers the sequence by creating a linked list. Any copies of this sequence
 * will have a pointer to one of the elements in the linked list, advancing the pointer
 * or this sequence to obtain the next element.
 * <p>
 * Elements that are not referenced by any copy are eventually garbage collected.
 *
 * @param <T> the type of elements in the sequence (covariant)
 */
public final class CopyableSeq<T> implements Seq<T>, PeekableSeq<T> {
    /** The current element pointed to by the copyable sequence. */
    private BufferElement<T> element;

    /**
     * Initializes a new instance of the {@link CopyableSeq} class.
     * <p>
     * Do not call this constructor. Instead, call {@link Seq#copyable()} to create a copyable sequence,
     * and {@link #copy()} to create each copy.
     *
     * @param seq the sequence
     */
    CopyableSeq(Seq<T> seq) {
        if (seq instanceof CopyableSeq) {
            this.element = ((CopyableSeq<T>)seq).element;
        } else {
            this.element = new BufferElement<>(null, seq);
        }
    }

    /**
     * Creates a copy of this sequence from its current position.
     * <p>
     * The implementation will ensure the sequence is only evaluated once.
     *
     * @return the sequence copy
     */
    public CopyableSeq<T> copy() {
        return new CopyableSeq<>(this);
    }

    @Override
    public T getCurrent() {
        return element.getValue();
    }

    @Override
    public boolean next() throws InterruptedException {
        element = element.next();
        return element != null;
    }

    @Override
    public CopyableSeq<T> copyable() {
        return this;
    }

    @Override
    public boolean peek() throws InterruptedException {
        // This will force a computation of the next element without advancing the pointer.
        return element.next() != null;
    }

    @Override
    public PeekableSeq<T> peekable() {
        return this;
    }

    /**
     * A linked-list element with a current value and pointing to an object that represents the next value.
     *
     * @param <T> the type of elements in the sequence (covariant)
     */
    private static final class BufferElement<T> {
        /** The value of this element. */
        private final T value;
        /**
         * Either a {@link BufferElement} with the next element in the linked list;
         * or a {@link Seq} that can compute the next element;
         * or an {@link InterruptedException} that will be thrown when the next element is requested;
         * or {@code null} if there is no next element.
         */
        @Nullable private Object next;
        /** Lock object. */
        private final Object lock = new Object();

        /**
         * A seq linked-list buffer element.
         *
         * @param value the value to store in this element, which may be {@code null} (e.g., when there is no valid value, at the start of a sequence)
         * @param seq the sequence that can compute the next element; or {@code null} if there is no next element
         */
        public BufferElement(T value, @Nullable Seq<T> seq) {
            this.value = value;
            this.next = seq;
        }

        /**
         * Gets the value of this element.
         * @return the value, which may be {@code null}
         */
        public T getValue() {
            return value;
        }

        /**
         * Gets the next element.
         *
         * @return the next element, either from the linked list buffer or newly computed;
         * or {@code null} if there are no more elements
         * @throws InterruptedException if the computation of the next element was interrupted
         */
        @SuppressWarnings("unchecked")
        @Nullable public BufferElement<T> next() throws InterruptedException {
            if (next == null) return null;
            else if (next instanceof BufferElement) return (BufferElement<T>)next;
            else if (next instanceof InterruptedException) throw (InterruptedException)next;

            synchronized(lock) {
                // In between the last condition checks and acquiring the lock,
                // another thread might have computed the next element.
                if (next == null) return null;
                else if (next instanceof BufferElement) return (BufferElement<T>)next;
                else if (next instanceof InterruptedException) throw (InterruptedException)next;

                // Compute the next element. Other threads will have to wait for this to complete.
                final Seq<T> seq = (Seq<T>)next;
                try {
                    if(!seq.next()) {
                        next = null;
                        return null;
                    } else {
                        final BufferElement<T> nextElement = new BufferElement<>(seq.getCurrent(), seq);
                        next = nextElement;
                        return nextElement;
                    }
                } catch (InterruptedException e) {
                    next = e;
                    throw e;
                }
            }
        }
    }
}
