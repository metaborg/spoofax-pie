package mb.spoofax.lwb.compiler.stratego;

import mb.cfg.Dependency;
import mb.cfg.metalang.CfgStrategoConfig;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.List;

public class SpoofaxStrategoConfig implements Serializable {
    public final CfgStrategoConfig cfgStrategoConfig;
    public final List<Dependency> dependencies;

    public SpoofaxStrategoConfig(CfgStrategoConfig cfgStrategoConfig, List<Dependency> dependencies) {
        this.cfgStrategoConfig = cfgStrategoConfig;
        this.dependencies = dependencies;
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final SpoofaxStrategoConfig that = (SpoofaxStrategoConfig)o;
        if(!cfgStrategoConfig.equals(that.cfgStrategoConfig)) return false;
        return dependencies.equals(that.dependencies);
    }

    @Override public int hashCode() {
        int result = cfgStrategoConfig.hashCode();
        result = 31 * result + dependencies.hashCode();
        return result;
    }

    @Override public String toString() {
        return "SpoofaxStrategoConfig{" +
            "cfgStrategoConfig=" + cfgStrategoConfig +
            ", dependencies=" + dependencies +
            '}';
    }
}
