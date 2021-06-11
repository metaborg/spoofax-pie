package mb.spt.api.model;

import mb.common.region.Region;
import mb.common.util.ListView;
import org.checkerframework.checker.nullness.qual.Nullable;

public class Fragment {
    public final Region region;
    public final ListView<Region> selections;
    public final ListView<FragmentPart> parts;

    public Fragment(Region region, ListView<Region> selections, ListView<FragmentPart> parts) {
        this.region = region;
        this.selections = selections;
        this.parts = parts;
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final Fragment fragment = (Fragment)o;
        if(!region.equals(fragment.region)) return false;
        if(!selections.equals(fragment.selections)) return false;
        return parts.equals(fragment.parts);
    }

    @Override public int hashCode() {
        int result = region.hashCode();
        result = 31 * result + selections.hashCode();
        result = 31 * result + parts.hashCode();
        return result;
    }

    @Override public String toString() {
        return "Fragment{" +
            "region=" + region +
            ", selections=" + selections +
            ", parts=" + parts +
            '}';
    }
}
