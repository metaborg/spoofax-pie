package mb.spoofax.runtime.impl.legacy;

import org.metaborg.core.source.ISourceRegion;
import org.metaborg.core.source.SourceRegion;

import mb.spoofax.runtime.model.region.Region;
import mb.spoofax.runtime.model.region.RegionImpl;

public class RegionConverter {
    public static Region toRegion(ISourceRegion sourceRegion) {
        return new RegionImpl(sourceRegion.startOffset(), sourceRegion.endOffset());
    }

    public static ISourceRegion toSourceRegion(Region region) {
        return new SourceRegion(region.startOffset(), region.endOffset());
    }
}
