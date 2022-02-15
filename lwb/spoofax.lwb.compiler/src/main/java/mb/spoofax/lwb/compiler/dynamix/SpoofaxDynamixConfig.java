package mb.spoofax.lwb.compiler.dynamix;

import mb.common.option.Option;
import mb.common.util.ADT;
import mb.dynamix.task.DynamixConfig;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

/**
 * Configuration for Dynamix in the context of the Spoofax LWB compiler.
 */
@ADT
public abstract class SpoofaxDynamixConfig implements Serializable {
    public interface Cases<R> {
        R files(DynamixConfig dynamixConfig, ResourcePath outputSpecAtermDirectory);

        R prebuilt(ResourcePath inputSpecAtermDirectory, ResourcePath outputSpecAtermDirectory);
    }

    public static SpoofaxDynamixConfig files(DynamixConfig dynamixConfig, ResourcePath outputSpecAtermDirectory) {
        return SpoofaxDynamixConfigs.files(dynamixConfig, outputSpecAtermDirectory);
    }

    public static SpoofaxDynamixConfig prebuilt(ResourcePath inputSpecAtermDirectory, ResourcePath outputSpecAtermDirectory) {
        return SpoofaxDynamixConfigs.prebuilt(inputSpecAtermDirectory, outputSpecAtermDirectory);
    }


    public abstract <R> R match(SpoofaxDynamixConfig.Cases<R> cases);

    public static SpoofaxDynamixConfigs.CasesMatchers.TotalMatcher_Files cases() {
        return SpoofaxDynamixConfigs.cases();
    }

    public SpoofaxDynamixConfigs.CaseOfMatchers.TotalMatcher_Files caseOf() {
        return SpoofaxDynamixConfigs.caseOf(this);
    }


    public Option<DynamixConfig> getDynamixConfig() {
        return Option.ofOptional(SpoofaxDynamixConfigs.getDynamixConfig(this));
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
