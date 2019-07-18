package mb.spoofax.core.language.transform;

import org.checkerframework.checker.nullness.qual.Nullable;

public class NoneSubject implements TransformSubject {
    @Override public void accept(TransformSubjectVisitor visitor) {
        visitor.none();
    }


    @Override public boolean equals(@Nullable Object obj) {
        return this == obj || (obj instanceof NoneSubject);
    }

    @Override public int hashCode() {
        return 0;
    }

    @Override public String toString() {
        return "None";
    }
}
