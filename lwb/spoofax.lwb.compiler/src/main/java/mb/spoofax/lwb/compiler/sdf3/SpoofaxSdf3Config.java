package mb.spoofax.lwb.compiler.sdf3;

import mb.common.option.Option;
import mb.common.util.ADT;
import mb.common.util.ListView;
import mb.resource.hierarchical.ResourcePath;
import mb.sdf3.task.spec.Sdf3SpecConfig;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

/**
 * Configuration for SDF3 in the context of the Spoofax LWB compiler.
 */
@ADT
public abstract class SpoofaxSdf3Config implements Serializable {
    public static class BuildParseTable implements Serializable {
        public final Sdf3SpecConfig sdf3SpecConfig;
        public final ResourcePath outputParseTableAtermFile;
        public final ResourcePath outputParseTablePersistedFile;

        public BuildParseTable(Sdf3SpecConfig sdf3SpecConfig, ResourcePath outputParseTableAtermFile, ResourcePath outputParseTablePersistedFile) {
            this.sdf3SpecConfig = sdf3SpecConfig;
            this.outputParseTableAtermFile = outputParseTableAtermFile;
            this.outputParseTablePersistedFile = outputParseTablePersistedFile;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final BuildParseTable that = (BuildParseTable)o;
            if(!sdf3SpecConfig.equals(that.sdf3SpecConfig)) return false;
            if(!outputParseTableAtermFile.equals(that.outputParseTableAtermFile)) return false;
            return outputParseTablePersistedFile.equals(that.outputParseTablePersistedFile);
        }

        @Override public int hashCode() {
            int result = sdf3SpecConfig.hashCode();
            result = 31 * result + outputParseTableAtermFile.hashCode();
            result = 31 * result + outputParseTablePersistedFile.hashCode();
            return result;
        }

        @Override public String toString() {
            return "BuildParseTable{" +
                "sdf3SpecConfig=" + sdf3SpecConfig +
                ", outputParseTableAtermFile=" + outputParseTableAtermFile +
                ", outputParseTablePersistedFile=" + outputParseTablePersistedFile +
                '}';
        }
    }

    public interface Cases<R> {
        R files(BuildParseTable mainBuildParseTable, ListView<BuildParseTable> otherBuildParseTables);

        R prebuilt(ResourcePath inputParseTableAtermFile, ResourcePath inputParseTablePersistedFile, ResourcePath outputParseTableAtermFile, ResourcePath outputParseTablePersistedFile);
    }

    public static SpoofaxSdf3Config files(BuildParseTable mainBuildParseTable, ListView<BuildParseTable> otherBuildParseTables) {
        return SpoofaxSdf3Configs.files(mainBuildParseTable, otherBuildParseTables);
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


    public Option<BuildParseTable> getMainBuildParseTable() {
        return Option.ofOptional(SpoofaxSdf3Configs.getMainBuildParseTable(this));
    }

    public Option<ListView<BuildParseTable>> getOtherBuildParseTables() {
        return Option.ofOptional(SpoofaxSdf3Configs.getOtherBuildParseTables(this));
    }

    public Option<Sdf3SpecConfig> getMainSdf3SpecConfig() {
        return getMainBuildParseTable().map(b -> b.sdf3SpecConfig);
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
