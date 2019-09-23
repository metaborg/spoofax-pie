package mb.spoofax.generator;

import mb.common.util.ADT;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Optional;

@ADT
public abstract class ClassId implements Serializable {
    interface Cases<R> {
        R generated(String generatedClassId);

        R custom(String customClassId);

        R customExtendingGenerated(String customClassId, String generatedClassId);
    }

    public abstract <R> R match(ClassId.Cases<R> cases);


    public String getClassId() {
        return caseOf().generated((id) -> id).custom((id) -> id).customExtendingGenerated((id, _ignore) -> id);
    }

    public Optional<String> getGeneratedClassId() {
        return ClassIds.getGeneratedClassId(this);
    }

    public Optional<String> getCustomClassId() {
        return ClassIds.getCustomClassId(this);
    }

    public boolean isGenerated() {
        return caseOf().custom_(false).otherwise_(true);
    }

    public boolean isCustom() {
        return caseOf().generated_(false).otherwise_(true);
    }

    public boolean isCustomExtendingGenerated() {
        return caseOf().customExtendingGenerated_(true).otherwise_(false);
    }

    public ClassIds.CaseOfMatchers.TotalMatcher_Generated caseOf() {
        return ClassIds.caseOf(this);
    }


    public ClassId generated(String generatedClassId) { return ClassIds.generated(generatedClassId);}


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}

