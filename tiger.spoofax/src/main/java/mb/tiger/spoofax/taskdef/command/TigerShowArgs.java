package mb.tiger.spoofax.taskdef.command;

import mb.common.region.Region;
import mb.common.util.CollectionView;
import mb.common.util.ListView;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.command.arg.ArgProviders;
import mb.spoofax.core.language.command.arg.ParamDef;
import mb.spoofax.core.language.command.arg.Params;
import mb.spoofax.core.language.command.arg.RawArgs;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Objects;

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
        return new ParamDef(CollectionView.of(
            Params.positional(0, ResourceKey.class, true, ListView.of(ArgProviders.context())),
            Params.option("region", Region.class, false, ListView.of(ArgProviders.context()))
        ));
    }


    @Override public boolean equals(@Nullable Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;
        final TigerShowArgs other = (TigerShowArgs) obj;
        return key.equals(other.key) &&
            Objects.equals(region, other.region);
    }

    @Override public int hashCode() {
        return Objects.hash(key, region);
    }

    @Override public String toString() {
        return key.toString() + (region != null ? "@" + region : "");
    }
}
