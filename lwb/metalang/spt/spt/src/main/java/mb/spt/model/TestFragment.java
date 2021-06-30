package mb.spt.model;

import mb.common.region.Region;
import mb.common.text.FragmentedString;
import mb.common.text.StringFragment;
import mb.common.text.Text;
import mb.common.util.ListView;

public interface TestFragment {
    Region getRegion();

    ListView<Region> getSelections();

    FragmentedString getFragmentedString();

    Text asText();

    String asString();
}
