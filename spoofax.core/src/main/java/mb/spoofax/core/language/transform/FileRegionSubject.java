package mb.spoofax.core.language.transform;

import mb.common.region.Region;
import mb.resource.ResourceKey;

public class FileRegionSubject extends ResourceSubject {
    private final Region region;

    public FileRegionSubject(ResourceKey resourceKey, Region region) {
        super(resourceKey);
        this.region = region;
    }

    public ResourceKey getFile() {
        return resourceKey;
    }

    public Region getRegion() {
        return region;
    }

    @Override public void accept(TransformSubjectVisitor visitor) {
        visitor.fileRegion(resourceKey, region);
    }

    @Override public String toString() {
        return resourceKey.toString() + "@" + region.toString();
    }
}
