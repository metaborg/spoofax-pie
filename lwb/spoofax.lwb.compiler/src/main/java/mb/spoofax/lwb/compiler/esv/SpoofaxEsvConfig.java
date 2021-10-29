package mb.spoofax.lwb.compiler.esv;

import mb.common.option.Option;
import mb.common.util.ADT;
import mb.esv.task.EsvConfig;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

/**
 * Configuration for ESV in the context of the Spoofax LWB compiler.
 */
@ADT
public abstract class SpoofaxEsvConfig implements Serializable {
    public interface Cases<R> {
        R files(EsvConfig esvConfig, ResourcePath outputFile);

        R prebuilt(ResourcePath inputFile, ResourcePath outputFile);
    }

    public static SpoofaxEsvConfig files(EsvConfig esvConfig, ResourcePath outputFile) {
        return SpoofaxEsvConfigs.files(esvConfig, outputFile);
    }

    public static SpoofaxEsvConfig prebuilt(ResourcePath inputFile, ResourcePath outputFile) {
        return SpoofaxEsvConfigs.prebuilt(inputFile, outputFile);
    }


    public abstract <R> R match(SpoofaxEsvConfig.Cases<R> cases);

    public static SpoofaxEsvConfigs.CasesMatchers.TotalMatcher_Files cases() {
        return SpoofaxEsvConfigs.cases();
    }

    public SpoofaxEsvConfigs.CaseOfMatchers.TotalMatcher_Files caseOf() {
        return SpoofaxEsvConfigs.caseOf(this);
    }


    public Option<EsvConfig> getEsvConfig() {
        return Option.ofOptional(SpoofaxEsvConfigs.getEsvConfig(this));
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
