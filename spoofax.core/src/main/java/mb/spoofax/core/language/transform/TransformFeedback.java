package mb.spoofax.core.language.transform;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

public interface TransformFeedback extends Serializable {
    void accept(TransformFeedbackVisitor visitor);

    @Override boolean equals(@Nullable Object obj);

    @Override int hashCode();

    @Override String toString();
}
