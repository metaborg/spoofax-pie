package mb.cfg;

import mb.common.util.SetView;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

public class Dependency implements Serializable {
    public final DependencySource source;
    public final SetView<DependencyKind> kinds;

    public Dependency(DependencySource source, SetView<DependencyKind> kinds) {
        this.source = source;
        this.kinds = kinds;
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final Dependency that = (Dependency)o;
        if(!source.equals(that.source)) return false;
        return kinds.equals(that.kinds);
    }

    @Override public int hashCode() {
        int result = source.hashCode();
        result = 31 * result + kinds.hashCode();
        return result;
    }

    @Override public String toString() {
        return source + " = " + kinds;
    }
}
