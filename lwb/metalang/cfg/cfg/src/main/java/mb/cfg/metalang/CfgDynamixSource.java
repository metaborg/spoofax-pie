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
 * Configuration for Dynamix sources in the context of CFG.
 */
@ADT @Value.Enclosing
public abstract class CfgDynamixSource implements Serializable {
    @Value.Immutable
    public interface Files extends Serializable {
        class Builder extends ImmutableCfgDynamixSource.Files.Builder {
            public static ResourcePath getDefaultMainSourceDirectory(CompileLanguageSpecificationShared shared) {
                return shared.languageProject().project().srcDirectory();
            }
        }

        static Builder builder() {return new Builder();}


        @Value.Default default ResourcePath mainSourceDirectory() {
            return Builder.getDefaultMainSourceDirectory(compileLanguageShared());
        }

        @Value.Default default ResourcePath mainFile() {
            return mainSourceDirectory().appendRelativePath("main.stx");
        }

        @Value.Default default ResourcePath generatedSourcesDirectory() {
            return compileLanguageShared().generatedSourcesDirectory().appendRelativePath("dynamix");
        }

        List<ResourcePath> includeDirectories();

        /// Automatically provided sub-inputs

        CompileLanguageSpecificationShared compileLanguageShared();
    }

    interface Cases<R> {
        R files(Files files);

        R prebuilt(ResourcePath specAtermDirectory);
    }

    public static CfgDynamixSource files(Files files) {
        return CfgDynamixSources.files(files);
    }

    public static CfgDynamixSource prebuilt(ResourcePath specAtermDirectory) {
        return CfgDynamixSources.prebuilt(specAtermDirectory);
    }


    public abstract <R> R match(Cases<R> cases);

    public static CfgDynamixSources.CasesMatchers.TotalMatcher_Files cases() {
        return CfgDynamixSource.cases();
    }

    public CfgDynamixSources.CaseOfMatchers.TotalMatcher_Files caseOf() {
        return CfgDynamixSources.caseOf(this);
    }

    public Optional<Files> getFiles() {
        return CfgDynamixSources.getFiles(this);
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
