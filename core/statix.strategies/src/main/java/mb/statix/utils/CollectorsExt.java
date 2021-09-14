package mb.statix.utils;

import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Extra {@link Collector} objects.
 */
public final class CollectorsExt {
    private CollectorsExt() { /* Cannot be instantated. */ }

    /**
     * Creates a map from a stream of {@link Map.Entry} objects.
     *
     * @param <K> the type of keys
     * @param <U> the type of values
     * @return the resulting map
     */
    public static <K, U> Collector<? super Map.Entry<K, U>, ?, Map<K, U>> toMap() {
        return Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue);
    }
}
