package mb.spoofax.lwb.compiler.statix;

import mb.common.option.Option;
import mb.common.util.ADT;
import mb.resource.hierarchical.ResourcePath;
import mb.statix.task.StatixConfig;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

/**
 * Configuration for Statix in the context of the Spoofax LWB compiler.
 */
@ADT
public abstract class SpoofaxStatixConfig implements Serializable {
    public interface Cases<R> {
        R files(StatixConfig statixConfig, ResourcePath outputSpecAtermDirectory);

        R prebuilt(ResourcePath inputSpecAtermDirectory, ResourcePath outputSpecAtermDirectory);
    }

    public static SpoofaxStatixConfig files(StatixConfig statixConfig, ResourcePath outputSpecAtermDirectory) {
        return SpoofaxStatixConfigs.files(statixConfig, outputSpecAtermDirectory);
    }

    public static SpoofaxStatixConfig prebuilt(ResourcePath inputSpecAtermDirectory, ResourcePath outputSpecAtermDirectory) {
        return SpoofaxStatixConfigs.prebuilt(inputSpecAtermDirectory, outputSpecAtermDirectory);
    }


    public abstract <R> R match(SpoofaxStatixConfig.Cases<R> cases);

    public static SpoofaxStatixConfigs.CasesMatchers.TotalMatcher_Files cases() {
        return SpoofaxStatixConfigs.cases();
    }

    public SpoofaxStatixConfigs.CaseOfMatchers.TotalMatcher_Files caseOf() {
        return SpoofaxStatixConfigs.caseOf(this);
    }


    public Option<StatixConfig> getStatixConfig() {
        return Option.ofOptional(SpoofaxStatixConfigs.getStatixConfig(this));
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
