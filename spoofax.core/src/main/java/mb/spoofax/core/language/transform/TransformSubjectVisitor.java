package mb.spoofax.core.language.transform;

import mb.common.region.Region;
import mb.resource.ResourceKey;

public interface TransformSubjectVisitor {
    void none();

    void project(ResourceKey project);

    void directory(ResourceKey directory);

    void file(ResourceKey file);

    void fileRegion(ResourceKey file, Region region);
}
