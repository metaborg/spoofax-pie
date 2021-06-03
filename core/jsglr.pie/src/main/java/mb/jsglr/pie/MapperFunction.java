package mb.jsglr.pie;

import mb.pie.api.SerializableFunction;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

abstract class MapperFunction<T, R extends Serializable> implements SerializableFunction<T, R> {
    @Override public boolean equals(@Nullable Object other) {
        return this == other || other != null && this.getClass() == other.getClass();
    }

    @Override public int hashCode() { return 0; }

    @Override public String toString() { return getClass().getSimpleName(); }
}
