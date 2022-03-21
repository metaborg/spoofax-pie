package mb.cfg.metalang;

import mb.cfg.CompileLanguageSpecificationShared;
import mb.common.util.ADT;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for Stratego sources in the context of CFG.
 */
@ADT @Value.Enclosing
public abstract class CfgStrategoSource implements Serializable {
    @Value.Immutable
    public interface Files extends Serializable {
        class Builder extends ImmutableCfgStrategoSource.Files.Builder {
            public static ResourcePath getDefaultMainSourceDirectory(CompileLanguageSpecificationShared shared) {
                return shared.languageProject().project().srcDirectory();
            }
        }

        static Builder builder() {return new Builder();}


        @Value.Default default ResourcePath mainSourceDirectory() {
            return Builder.getDefaultMainSourceDirectory(compileLanguageShared());
        }

        @Value.Default default ResourcePath mainFile() {
            return mainSourceDirectory().appendRelativePath("main.str2");
        }

        @Value.Default default String mainModule() {
            return "main";
        }

        List<ResourcePath> includeDirectories();

        @Value.Default default List<String> includeBuiltinLibraries() {
            final ArrayList<String> strategoBuiltinLibs = new ArrayList<>();
            strategoBuiltinLibs.add("libstratego-sglr");
            strategoBuiltinLibs.add("libstratego-aterm");
            return strategoBuiltinLibs;
        }

        List<String> exportDirectories();

        default ResourcePath strategoLibUnarchiveDirectory() {
            return compileLanguageShared().unarchiveDirectory().appendRelativePath("strategoLib");
        }

        default ResourcePath gppUnarchiveDirectory() {
            return compileLanguageShared().unarchiveDirectory().appendRelativePath("gpp");
        }

        @Value.Default default boolean includeLibSpoofax2Exports() {
            return compileLanguageShared().includeLibSpoofax2Exports();
        }

        default ResourcePath libSpoofax2UnarchiveDirectory() {
            return compileLanguageShared().libSpoofax2UnarchiveDirectory();
        }

        @Value.Default default boolean includeLibStatixExports() {
            return compileLanguageShared().includeLibStatixExports();
        }

        default ResourcePath libStatixUnarchiveDirectory() {
            return compileLanguageShared().libStatixUnarchiveDirectory();
        }

        @Value.Default default ResourcePath generatedSourcesDirectory() {
            return compileLanguageShared().generatedSourcesDirectory().appendRelativePath("stratego");
        }

        @Value.Default default ResourcePath cacheDirectory() {
            return compileLanguageShared().languageProject().project().buildDirectory().appendRelativePath("stratego-cache");
        }

        /// Automatically provided sub-inputs

        CompileLanguageSpecificationShared compileLanguageShared();
    }

    interface Cases<R> {
        R files(Files files);
    }

    public static CfgStrategoSource files(Files files) {
        return CfgStrategoSources.files(files);
    }


    public abstract <R> R match(Cases<R> cases);

    public static CfgStrategoSources.CasesMatchers.TotalMatcher_Files cases() {
        return CfgStrategoSources.cases();
    }

//    public CfgStrategoSources.CaseOfMatchers.TotalMatcher_Files caseOf() {
//        return CfgStrategoSources.caseOf(this);
//    }

    public Files getFiles() {
        return CfgStrategoSources.getFiles(this);
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
