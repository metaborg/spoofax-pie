package mb.spt.model;

import mb.common.region.Region;
import mb.common.text.FragmentedString;
import mb.common.text.StringFragment;
import mb.common.text.Text;
import mb.common.util.ListView;

public interface TestFragment {
    Region getRegion();

    /**
     * @return regions of the selections relative to the start of the SPT file
     */
    ListView<Region> getSelections();

    /**
     * @return regions of the selections relative to the start of the fragment
     */
    ListView<Region> getInFragmentSelections();

    FragmentedString getFragmentedString();

    Text asText();

    String asString();
}
