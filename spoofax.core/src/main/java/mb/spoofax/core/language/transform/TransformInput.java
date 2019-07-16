package mb.spoofax.core.language.transform;

import java.io.Serializable;
import java.util.Objects;

public class TransformInput implements Serializable {
    public final TransformSubject subject;

    public TransformInput(TransformSubject subject) {
        this.subject = subject;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final TransformInput that = (TransformInput) o;
        return subject.equals(that.subject);
    }

    @Override public int hashCode() {
        return Objects.hash(subject);
    }

    @Override public String toString() {
        return subject.toString();
    }
}
