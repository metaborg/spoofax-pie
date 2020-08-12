package mb.statix.multilang.metadata;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Objects;

public abstract class AbstractId implements Serializable {

    private final String id;

    protected AbstractId(String id) {
        this.id = id;
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        AbstractId Id1 = (AbstractId)o;
        return Objects.equals(id, Id1.id);
    }

    @Override public int hashCode() {
        return Objects.hash(id);
    }

    @Override public String toString() {
        return id;
    }

    public String getId() {
        return id;
    }
}
