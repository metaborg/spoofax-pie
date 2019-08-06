package mb.spoofax.eclipse.transform;

import mb.common.util.ListView;
import mb.spoofax.core.language.transform.TransformContext;
import mb.spoofax.core.language.transform.TransformExecutionType;

import java.io.Serializable;
import java.util.Objects;

public class TransformData implements Serializable {
    public final String transformId;
    public final TransformExecutionType executionType;
    public final ListView<TransformContext> contexts;

    public TransformData(String transformId, TransformExecutionType executionType, ListView<TransformContext> contexts) {
        this.transformId = transformId;
        this.executionType = executionType;
        this.contexts = contexts;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final TransformData that = (TransformData) o;
        return transformId.equals(that.transformId) &&
            executionType == that.executionType &&
            contexts.equals(that.contexts);
    }

    @Override public int hashCode() {
        return Objects.hash(transformId, executionType, contexts);
    }

    @Override public String toString() {
        return "TransformData(" +
            "transformId='" + transformId + '\'' +
            ", executionType=" + executionType +
            ", contexts=" + contexts +
            ')';
    }
}
