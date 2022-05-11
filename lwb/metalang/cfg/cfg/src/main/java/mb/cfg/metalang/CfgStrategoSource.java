package mb.cfg.metalang;

import mb.cfg.CompileMetaLanguageSourcesShared;
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
            public static ResourcePath getDefaultMainSourceDirectory(CompileMetaLanguageSourcesShared shared) {
                return shared.languageProject().project().srcDirectory();
            }
        }

        static Builder builder() {return new Builder();}


        @Value.Default default ResourcePath mainSourceDirectory() {
            return Builder.getDefaultMainSourceDirectory(compileMetaLanguageSourcesShared());
        }

        @Value.Default default ResourcePath mainFile() {
            return mainSourceDirectory().appendRelativePath("main.str2");
        }

        @Value.Default default String mainModule() {
            return "main";
        }

        List<ResourcePath> includeDirectories();

        List<String> exportDirectories();

        @Value.Default default List<String> includeBuiltinLibraries() {
            final ArrayList<String> strategoBuiltinLibs = new ArrayList<>();
            strategoBuiltinLibs.add("libstratego-sglr");
            strategoBuiltinLibs.add("libstratego-aterm");
            return strategoBuiltinLibs;
        }

        default ResourcePath unarchiveDirectory() {
            return compileMetaLanguageSourcesShared().unarchiveDirectory();
        }

        default ResourcePath strategoLibUnarchiveDirectory() {
            return unarchiveDirectory().appendRelativePath("strategoLib");
        }

        default ResourcePath gppUnarchiveDirectory() {
            return unarchiveDirectory().appendRelativePath("gpp");
        }

        @Value.Default default boolean includeLibSpoofax2Exports() {
            return compileMetaLanguageSourcesShared().includeLibSpoofax2Exports();
        }

        default ResourcePath libSpoofax2UnarchiveDirectory() {
            return compileMetaLanguageSourcesShared().libSpoofax2UnarchiveDirectory();
        }

        @Value.Default default boolean includeLibStatixExports() {
            return compileMetaLanguageSourcesShared().includeLibStatixExports();
        }

        default ResourcePath libStatixUnarchiveDirectory() {
            return compileMetaLanguageSourcesShared().libStatixUnarchiveDirectory();
        }

        @Value.Default default ResourcePath generatedSourcesDirectory() {
            return compileMetaLanguageSourcesShared().generatedSourcesDirectory().appendRelativePath("stratego");
        }

        @Value.Default default ResourcePath cacheDirectory() {
            return compileMetaLanguageSourcesShared().languageProject().project().buildDirectory().appendRelativePath("stratego-cache");
        }

        /// Automatically provided sub-inputs

        CompileMetaLanguageSourcesShared compileMetaLanguageSourcesShared();
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

    public Files getFiles() {
        return CfgStrategoSources.getFiles(this);
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
