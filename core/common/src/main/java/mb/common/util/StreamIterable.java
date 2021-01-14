package mb.common.util;

import java.util.Iterator;
import java.util.stream.Stream;

/**
 * An {@link Iterable} that wraps a stream. Because streams can only be iterated once, calling {@link #iterator()} more
 * than once may result in an {@link IllegalStateException} being thrown.
 *
 * @param <T> Type of elements.
 */
public class StreamIterable<T> implements Iterable<T> {
    private final Stream<T> stream;

    public StreamIterable(Stream<T> stream) {
        this.stream = stream;
    }

    @Override public Iterator<T> iterator() {
        return stream.iterator();
    }
}
