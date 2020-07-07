package mb.common.region;

import mb.common.util.ADT;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Optional;

@ADT
public abstract class Selection implements Serializable {
    interface Cases<R> {
        R region(Region region);

        R offset(int offset);
    }

    public static Selection region(Region region) {
        return Selections.region(region);
    }

    public static Selection offset(int offset) {
        return Selections.offset(offset);
    }


    public abstract <R> R match(Cases<R> cases);

    public Optional<Region> getRegion() {
        return Selections.getRegion(this);
    }

    public Optional<Integer> getOffset() {
        return Selections.getOffset(this);
    }

    public Selections.CaseOfMatchers.TotalMatcher_Region caseOf() {
        return Selections.caseOf(this);
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
