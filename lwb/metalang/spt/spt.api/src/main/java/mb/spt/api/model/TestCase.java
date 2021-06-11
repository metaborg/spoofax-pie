package mb.spt.api.model;

import mb.common.region.Region;
import mb.common.util.ListView;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

public class TestCase {
    public final ResourceKey file;
    public final @Nullable ResourcePath rootDirectoryHint;
    public final String description;
    public final Region descriptionRegion;
    public final Fragment fragment;
    public final ListView<TestExpectation> expectations;

    public TestCase(
        ResourceKey file,
        @Nullable ResourcePath rootDirectoryHint,
        String description,
        Region descriptionRegion,
        Fragment fragment,
        ListView<TestExpectation> expectations
    ) {
        this.file = file;
        this.rootDirectoryHint = rootDirectoryHint;
        this.description = description;
        this.descriptionRegion = descriptionRegion;
        this.fragment = fragment;
        this.expectations = expectations;
    }

}
