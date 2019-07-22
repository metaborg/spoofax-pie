package mb.common.region;

import mb.common.util.ADT;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

@ADT
public abstract class Selection implements Serializable {
    interface Cases<R> {
        R region(Region region);

        R offset(int offset);

        R none();
    }

    public abstract <R> R match(Cases<R> cases);

    public boolean isRegion() {
        return Selections.caseOf(this).region_(true).otherwise_(false);
    }

    public boolean isOffset() {
        return Selections.caseOf(this).offset_(true).otherwise_(false);
    }

    public boolean isNone() {
        return Selections.caseOf(this).none_(true).otherwise_(false);
    }

    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
