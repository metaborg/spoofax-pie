package mb.pipe.run.spoofax.util;

import org.metaborg.core.source.ISourceRegion;
import org.metaborg.core.source.SourceRegion;

import mb.pipe.run.core.model.region.Region;
import mb.pipe.run.core.model.region.RegionImpl;

public class RegionConverter {
    public static Region toRegion(ISourceRegion sourceRegion) {
        return new RegionImpl(sourceRegion.startOffset(), sourceRegion.endOffset());
    }

    public static ISourceRegion toSourceRegion(Region region) {
        return new SourceRegion(region.startOffset(), region.endOffset());
    }
}
