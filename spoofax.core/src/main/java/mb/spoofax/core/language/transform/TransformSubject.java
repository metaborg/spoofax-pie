package mb.spoofax.core.language.transform;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

public interface TransformSubject extends Serializable {
    void accept(TransformSubjectVisitor visitor);

    @Override boolean equals(@Nullable Object obj);

    @Override int hashCode();

    @Override String toString();
}
