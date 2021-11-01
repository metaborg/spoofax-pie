package mb.spoofax.lwb.compiler.sdf3;

import mb.common.option.Option;
import mb.common.util.ADT;
import mb.resource.hierarchical.ResourcePath;
import mb.sdf3.task.spec.Sdf3SpecConfig;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

/**
 * Configuration for SDF3 in the context of the Spoofax LWB compiler.
 */
@ADT
public abstract class SpoofaxSdf3Config implements Serializable {
    public interface Cases<R> {
        R files(Sdf3SpecConfig sdf3SpecConfig, ResourcePath outputParseTableAtermFile, ResourcePath outputParseTablePersistedFile);

        R prebuilt(ResourcePath inputParseTableAtermFile, ResourcePath inputParseTablePersistedFile, ResourcePath outputParseTableAtermFile, ResourcePath outputParseTablePersistedFile);
    }

    public static SpoofaxSdf3Config files(Sdf3SpecConfig sdf3SpecConfig, ResourcePath outputParseTableAtermFile, ResourcePath outputParseTablePersistedFile) {
        return SpoofaxSdf3Configs.files(sdf3SpecConfig, outputParseTableAtermFile, outputParseTablePersistedFile);
    }

    public static SpoofaxSdf3Config prebuilt(ResourcePath inputParseTableAtermFile, ResourcePath inputParseTablePersistedFile, ResourcePath outputParseTableAtermFile, ResourcePath outputParseTablePersistedFile) {
        return SpoofaxSdf3Configs.prebuilt(inputParseTableAtermFile, inputParseTablePersistedFile, outputParseTableAtermFile, outputParseTablePersistedFile);
    }


    public abstract <R> R match(SpoofaxSdf3Config.Cases<R> cases);

    public static SpoofaxSdf3Configs.CasesMatchers.TotalMatcher_Files cases() {
        return SpoofaxSdf3Configs.cases();
    }

    public SpoofaxSdf3Configs.CaseOfMatchers.TotalMatcher_Files caseOf() {
        return SpoofaxSdf3Configs.caseOf(this);
    }


    public Option<Sdf3SpecConfig> getSdf3SpecConfig() {
        return Option.ofOptional(SpoofaxSdf3Configs.getSdf3SpecConfig(this));
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
