package mb.spt.model;

import mb.common.region.Region;
import mb.common.util.ListView;
import mb.spt.api.model.Fragment;
import mb.spt.api.model.FragmentPart;
import org.checkerframework.checker.nullness.qual.Nullable;

public class FragmentImpl implements Fragment {
    private final Region region;
    private final ListView<Region> selections;
    private final ListView<FragmentPart> parts;

    public FragmentImpl(Region region, ListView<Region> selections, ListView<FragmentPart> parts) {
        this.region = region;
        this.selections = selections;
        this.parts = parts;
    }

    @Override public Region getRegion() {
        return region;
    }

    @Override public ListView<Region> getSelections() {
        return selections;
    }

    @Override public ListView<FragmentPart> getParts() {
        return parts;
    }

    @Override public String asText() {
        final StringBuilder sb = new StringBuilder();
        for(FragmentPart part : parts) {
            // add whitespace to get the character offset of this piece right
            for(int i = sb.length(); i < part.startOffset; i++) {
                sb.append(" ");
            }
            // add the actual piece of program text from the fragment
            sb.append(part.text);
        }
        return sb.toString();
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final FragmentImpl fragment = (FragmentImpl)o;
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
