package mb.spoofax.runtime.cfg;

import java.io.Serializable;
import java.util.Objects;

public class LangId implements Serializable {
    public final String id;

    public LangId(String id) {
        this.id = id;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final LangId langId = (LangId) o;
        return Objects.equals(id, langId.id);
    }

    @Override public int hashCode() {
        return id.hashCode();
    }

    @Override public String toString() {
        return id;
    }
}
