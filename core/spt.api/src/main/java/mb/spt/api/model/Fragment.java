package mb.spt.api.model;

import mb.common.region.Region;
import mb.common.util.ListView;

public interface Fragment {
    Region getRegion();

    ListView<Region> getSelections();

    ListView<FragmentPart> getParts();

    String asText();
}
