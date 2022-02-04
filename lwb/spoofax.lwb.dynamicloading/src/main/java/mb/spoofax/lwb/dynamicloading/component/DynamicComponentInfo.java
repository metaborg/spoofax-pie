package mb.spoofax.lwb.dynamicloading.component;

import mb.common.util.SetView;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.Coordinate;

import java.io.Serializable;

public class DynamicComponentInfo implements Serializable {
    public final ResourcePath rootDirectory;
    public final Coordinate coordinate;
    public final String displayName;
    public final SetView<String> fileExtensions;

    public DynamicComponentInfo(ResourcePath rootDirectory, Coordinate coordinate, String displayName, SetView<String> fileExtensions) {
        this.rootDirectory = rootDirectory;
        this.coordinate = coordinate;
        this.displayName = displayName;
        this.fileExtensions = fileExtensions;
    }

    @Override public String toString() {
        return displayName;
    }
}
