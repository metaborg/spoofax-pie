package mb.cfg.metalang;

import mb.cfg.CompileMetaLanguageSourcesShared;
import mb.common.util.ADT;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * Configuration for SDF3 sources in the context of CFG.
 */
@ADT @Value.Enclosing
public abstract class CfgSdf3Source implements Serializable {
    @Value.Immutable
    public interface Files extends Serializable {
        class Builder extends ImmutableCfgSdf3Source.Files.Builder {
            public static ResourcePath getDefaultMainSourceDirectory(CompileMetaLanguageSourcesShared shared) {
                return shared.languageProject().project().srcDirectory();
            }
        }

        static Builder builder() {return new Builder();}


        @Value.Default default ResourcePath mainSourceDirectory() {
            return Builder.getDefaultMainSourceDirectory(compileMetaLanguageSourcesShared());
        }

        @Value.Default default ResourcePath mainFile() {
            return mainSourceDirectory().appendRelativePath("start.sdf3");
        }

        List<ResourcePath> includeDirectories();

        List<String> exportDirectories();

        default ResourcePath unarchiveDirectory() {
            return compileMetaLanguageSourcesShared().unarchiveDirectory();
        }

        /// Automatically provided sub-inputs

        CompileMetaLanguageSourcesShared compileMetaLanguageSourcesShared();
    }

    interface Cases<R> {
        R files(Files files);

        R prebuilt(ResourcePath inputParseTableAtermFile, ResourcePath inputParseTablePersistedFile);
    }

    public static CfgSdf3Source files(Files files) {
        return CfgSdf3Sources.files(files);
    }

    public static CfgSdf3Source prebuilt(ResourcePath inputParseTableAtermFile, ResourcePath inputParseTablePersistedFile) {
        return CfgSdf3Sources.prebuilt(inputParseTableAtermFile, inputParseTablePersistedFile);
    }


    public abstract <R> R match(Cases<R> cases);

    public static CfgSdf3Sources.CasesMatchers.TotalMatcher_Files cases() {
        return CfgSdf3Sources.cases();
    }

    public CfgSdf3Sources.CaseOfMatchers.TotalMatcher_Files caseOf() {
        return CfgSdf3Sources.caseOf(this);
    }

    public Optional<Files> getFiles() {
        return CfgSdf3Sources.getFiles(this);
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
