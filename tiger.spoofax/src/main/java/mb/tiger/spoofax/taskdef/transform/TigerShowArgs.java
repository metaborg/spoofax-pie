package mb.tiger.spoofax.taskdef.transform;

import mb.common.region.Region;
import mb.common.util.CollectionView;
import mb.common.util.ListView;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.transform.param.ArgSources;
import mb.spoofax.core.language.transform.param.ParamDef;
import mb.spoofax.core.language.transform.param.Params;
import mb.spoofax.core.language.transform.param.RawArgs;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

public class TigerShowArgs implements Serializable {
    public final ResourceKey key;
    public final @Nullable Region region;


    public TigerShowArgs(ResourceKey key, @Nullable Region region) {
        this.key = key;
        this.region = region;
    }


    public static TigerShowArgs fromRawArgs(RawArgs rawArgs) {
        final @Nullable ResourceKey key = rawArgs.getPositional(0);
        if(key == null) {
            throw new RuntimeException("Could not create arguments from raw arguments '" + rawArgs + "', it has no positional argument at index 0");
        }
        final @Nullable Region region = rawArgs.getOption("region");
        return new TigerShowArgs(key, region);
    }

    public static ParamDef getParamDef() {
        // TODO: need converters for ResourceKey and Region.
        return new ParamDef(CollectionView.of(
            Params.positional(0, ResourceKey.class, true, ListView.of(ArgSources.context())),
            Params.option("region", Region.class, false, ListView.of(ArgSources.context()))
        ));
    }
}
