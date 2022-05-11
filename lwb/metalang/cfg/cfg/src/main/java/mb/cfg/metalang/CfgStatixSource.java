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
 * Configuration for Statix sources in the context of CFG.
 */
@ADT @Value.Enclosing
public abstract class CfgStatixSource implements Serializable {
    @Value.Immutable
    public interface Files extends Serializable {
        class Builder extends ImmutableCfgStatixSource.Files.Builder {
            public static ResourcePath getDefaultMainSourceDirectory(CompileMetaLanguageSourcesShared shared) {
                return shared.languageProject().project().srcDirectory();
            }
        }

        static Builder builder() {return new Builder();}


        @Value.Default default ResourcePath mainSourceDirectory() {
            return Builder.getDefaultMainSourceDirectory(compileMetaLanguageSourcesShared());
        }

        @Value.Default default ResourcePath mainFile() {
            return mainSourceDirectory().appendRelativePath("main.stx");
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

        R prebuilt(ResourcePath specAtermDirectory);
    }

    public static CfgStatixSource files(Files files) {
        return CfgStatixSources.files(files);
    }

    public static CfgStatixSource prebuilt(ResourcePath specAtermDirectory) {
        return CfgStatixSources.prebuilt(specAtermDirectory);
    }


    public abstract <R> R match(Cases<R> cases);

    public static CfgStatixSources.CasesMatchers.TotalMatcher_Files cases() {
        return CfgStatixSources.cases();
    }

    public CfgStatixSources.CaseOfMatchers.TotalMatcher_Files caseOf() {
        return CfgStatixSources.caseOf(this);
    }

    public Optional<Files> getFiles() {
        return CfgStatixSources.getFiles(this);
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
