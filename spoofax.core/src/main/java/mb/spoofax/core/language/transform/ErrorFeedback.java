package mb.spoofax.core.language.transform;

import java.util.Objects;

public class ErrorFeedback implements TransformFeedback {
    private final Throwable error;

    public ErrorFeedback(Throwable error) {
        this.error = error;
    }

    public Throwable getError() {
        return error;
    }

    @Override public void accept(TransformFeedbackVisitor visitor) {
        visitor.error(error);
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final ErrorFeedback that = (ErrorFeedback) o;
        return error.equals(that.error);
    }

    @Override public int hashCode() {
        return Objects.hash(error);
    }

    @Override public String toString() {
        return error.toString();
    }
}
