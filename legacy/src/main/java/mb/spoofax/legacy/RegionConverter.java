package mb.spoofax.legacy;

import mb.spoofax.api.region.Region;
import org.metaborg.core.source.ISourceRegion;
import org.metaborg.core.source.SourceRegion;

public class RegionConverter {
    public static Region toRegion(ISourceRegion sourceRegion) {
        return new Region(sourceRegion.startOffset(), sourceRegion.endOffset());
    }

    public static ISourceRegion toSourceRegion(Region region) {
        return new SourceRegion(region.startOffset, region.endOffset);
    }
}
