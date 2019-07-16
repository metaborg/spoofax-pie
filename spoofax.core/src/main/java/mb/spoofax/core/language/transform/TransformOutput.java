package mb.spoofax.core.language.transform;

import mb.common.util.ListView;

import java.io.Serializable;
import java.util.Objects;

public class TransformOutput implements Serializable {
    public final ListView<TransformFeedback> feedback;

    public TransformOutput(ListView<TransformFeedback> feedback) {
        this.feedback = feedback;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final TransformOutput that = (TransformOutput) o;
        return feedback.equals(that.feedback);
    }

    @Override public int hashCode() {
        return Objects.hash(feedback);
    }

    @Override public String toString() {
        return feedback.toString();
    }
}
