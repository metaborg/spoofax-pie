package mb.statix.multilang;

import java.io.Serializable;
import java.util.Objects;

public class ContextId implements Serializable {

    private final String contextId;

    public ContextId(String contextId) {
        this.contextId = contextId;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        ContextId contextId1 = (ContextId)o;
        return Objects.equals(contextId, contextId1.contextId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contextId);
    }

    @Override public String toString() {
        return contextId;
    }
}
