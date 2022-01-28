package mb.spoofax.core;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

public class Coordinate implements Serializable {
    public final String groupId;
    public final String id;
    public final Version version;

    public Coordinate(String groupId, String id, Version version) {
        this.groupId = groupId;
        this.id = id;
        this.version = version;
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final Coordinate that = (Coordinate)o;
        if(!groupId.equals(that.groupId)) return false;
        if(!id.equals(that.id)) return false;
        return version.equals(that.version);
    }

    @Override public int hashCode() {
        int result = groupId.hashCode();
        result = 31 * result + id.hashCode();
        result = 31 * result + version.hashCode();
        return result;
    }
}
