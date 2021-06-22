package mb.spt.model;

import mb.common.region.Region;
import mb.common.text.FragmentedString;
import mb.common.text.Text;
import mb.common.util.ListView;
import mb.spt.api.model.TestFragment;
import org.checkerframework.checker.nullness.qual.Nullable;

public class TestFragmentImpl implements TestFragment {
    private final Region region;
    private final ListView<Region> selections;
    private final FragmentedString fragmentedString;

    public TestFragmentImpl(Region region, ListView<Region> selections, FragmentedString fragmentedString) {
        this.region = region;
        this.selections = selections;
        this.fragmentedString = fragmentedString;
    }

    @Override public Region getRegion() {
        return region;
    }

    @Override public ListView<Region> getSelections() {
        return selections;
    }

    @Override public FragmentedString getFragmentedString() {
        return fragmentedString;
    }

    @Override public String asString() {
        return fragmentedString.toString();
    }

    @Override public Text asText() {
        return Text.fragmentedString(fragmentedString);
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final TestFragmentImpl that = (TestFragmentImpl)o;
        if(!region.equals(that.region)) return false;
        if(!selections.equals(that.selections)) return false;
        return fragmentedString.equals(that.fragmentedString);
    }

    @Override public int hashCode() {
        int result = region.hashCode();
        result = 31 * result + selections.hashCode();
        result = 31 * result + fragmentedString.hashCode();
        return result;
    }

    @Override public String toString() {
        return "TestFragmentImpl{" +
            "region=" + region +
            ", selections=" + selections +
            ", fragmentedString=" + fragmentedString +
            '}';
    }
}
