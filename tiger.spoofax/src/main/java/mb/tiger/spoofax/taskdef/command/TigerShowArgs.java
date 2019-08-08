package mb.tiger.spoofax.taskdef.command;

import mb.common.region.Region;
import mb.common.util.ListView;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.cli.CliParamDef;
import mb.spoofax.core.language.cli.CliParams;
import mb.spoofax.core.language.command.arg.ArgProviders;
import mb.spoofax.core.language.command.arg.Param;
import mb.spoofax.core.language.command.arg.ParamDef;
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
        final ResourceKey resource = rawArgs.getOrThrow("resource");
        final @Nullable Region region = rawArgs.getOrNull("region");
        return new TigerShowArgs(resource, region);
    }

    public static ParamDef getParamDef() {
        return new ParamDef(
            Param.of("resource", ResourceKey.class, true, ArgProviders.context()),
            Param.of("region", Region.class, false, ArgProviders.context())
        );
    }

    public static CliParamDef getCliParamDef(String operationName) {
        return new CliParamDef(
            CliParams.positional("resource", 0, "resource", "Source file to " + operationName),
            CliParams.option("region", ListView.of("-r", "--region"), "region", "Region in source file to " + operationName)
        );
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
