package mb.spt.model;

import mb.common.region.Region;
import org.checkerframework.checker.nullness.qual.Nullable;

public class SelectionReference {
    public final int selection;
    public final Region region;

    public SelectionReference(int selection, Region region) {
        this.selection = selection;
        this.region = region;
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final SelectionReference that = (SelectionReference)o;
        if(selection != that.selection) return false;
        return region.equals(that.region);
    }

    @Override public int hashCode() {
        int result = selection;
        result = 31 * result + region.hashCode();
        return result;
    }

    @Override public String toString() {
        return "SelectionReference{" +
            "selection=" + selection +
            ", region=" + region +
            '}';
    }
}
