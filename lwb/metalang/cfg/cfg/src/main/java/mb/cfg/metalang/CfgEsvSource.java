package mb.cfg.metalang;

import mb.cfg.CompileLanguageSpecificationShared;
import mb.common.util.ADT;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * Configuration for ESV sources in the context of the CFG.
 */
@ADT @Value.Enclosing
public abstract class CfgEsvSource implements Serializable {
    @Value.Immutable
    public interface Files extends Serializable {
        class Builder extends ImmutableCfgEsvSource.Files.Builder {
            public static ResourcePath getDefaultMainSourceDirectory(CompileLanguageSpecificationShared shared) {
                return shared.languageProject().project().srcDirectory();
            }
        }

        static Builder builder() {return new Builder();}


        @Value.Default default ResourcePath mainSourceDirectory() {
            return Builder.getDefaultMainSourceDirectory(compileLanguageShared());
        }

        @Value.Default default ResourcePath mainFile() {
            return mainSourceDirectory().appendRelativePath("main.esv");
        }

        List<ResourcePath> includeDirectories();

        List<String> exportDirectories();

        @Value.Default default boolean includeLibSpoofax2Exports() {
            return compileLanguageShared().includeLibSpoofax2Exports();
        }

        @Value.Default default ResourcePath libSpoofax2UnarchiveDirectory() {
            return compileLanguageShared().libSpoofax2UnarchiveDirectory();
        }

        /// Automatically provided sub-inputs

        CompileLanguageSpecificationShared compileLanguageShared();
    }

    interface Cases<R> {
        R files(Files files);

        R prebuilt(ResourcePath esvAtermFile);
    }

    public static CfgEsvSource files(Files files) {
        return CfgEsvSources.files(files);
    }

    public static CfgEsvSource prebuilt(ResourcePath inputFile) {
        return CfgEsvSources.prebuilt(inputFile);
    }


    public abstract <R> R match(Cases<R> cases);

    public static CfgEsvSources.CasesMatchers.TotalMatcher_Files cases() {
        return CfgEsvSources.cases();
    }

    public CfgEsvSources.CaseOfMatchers.TotalMatcher_Files caseOf() {
        return CfgEsvSources.caseOf(this);
    }

    public Optional<Files> getFiles() {
        return CfgEsvSources.getFiles(this);
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
