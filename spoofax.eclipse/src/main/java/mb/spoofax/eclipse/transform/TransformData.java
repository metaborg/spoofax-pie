package mb.spoofax.eclipse.transform;

import mb.common.util.ListView;
import mb.spoofax.core.language.transform.TransformExecutionType;
import mb.spoofax.core.language.transform.TransformInput;

import java.io.Serializable;
import java.util.Objects;

public class TransformData implements Serializable {
    public final String transformId;
    public final TransformExecutionType executionType;
    public final ListView<TransformInput> inputs;

    public TransformData(String transformId, TransformExecutionType executionType, ListView<TransformInput> inputs) {
        this.transformId = transformId;
        this.executionType = executionType;
        this.inputs = inputs;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final TransformData that = (TransformData) o;
        return transformId.equals(that.transformId) &&
            executionType == that.executionType &&
            inputs.equals(that.inputs);
    }

    @Override public int hashCode() {
        return Objects.hash(transformId, executionType, inputs);
    }

    @Override public String toString() {
        return "TransformData(" +
            "transformId='" + transformId + '\'' +
            ", executionType=" + executionType +
            ", inputs=" + inputs +
            ')';
    }
}
