package mb.cfg.metalang;

import mb.cfg.CompileMetaLanguageSourcesShared;
import mb.common.util.ADT;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.adapter.ExportsCompiler;
import mb.spoofax.compiler.language.StrategoRuntimeLanguageCompiler;
import mb.spoofax.compiler.util.BuilderBase;
import mb.spoofax.compiler.util.Conversion;
import mb.spoofax.compiler.util.Shared;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Configuration for Stratego sources in the context of CFG.
 */
@ADT @Value.Enclosing
public abstract class CfgStrategoSource implements Serializable {
    @Value.Immutable
    public interface Files extends Serializable {
        class Builder extends ImmutableCfgStrategoSource.Files.Builder implements BuilderBase {
            static final String propertiesPrefix = "stratego.source.files.";
            static final String languageStrategyAffix = propertiesPrefix + "languageStrategyAffix";

            public Builder withPersistentProperties(Properties properties) {
                with(properties, languageStrategyAffix, this::languageStrategyAffix);
                return this;
            }

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

        @Value.Default default boolean enableSdf3StatixExplicationGen() {
            return false;
        }

        @Value.Default default String languageStrategyAffix() {
            // TODO: convert to Stratego ID instead of Java ID.
            return Conversion.nameToJavaId(shared().name().toLowerCase());
        }


        default ResourcePath unarchiveDirectory() {
            return compileMetaLanguageSourcesShared().unarchiveDirectory().appendAsRelativePath("stratego");
        }

        @Value.Default default ResourcePath generatedSourcesDirectory() {
            return compileMetaLanguageSourcesShared().generatedSourcesDirectory().appendRelativePath("stratego");
        }

        @Value.Default default ResourcePath cacheDirectory() {
            return compileMetaLanguageSourcesShared().cacheDirectory().appendAsRelativePath("stratego");
        }


        /// Automatically provided sub-inputs

        CompileMetaLanguageSourcesShared compileMetaLanguageSourcesShared();

        Shared shared();


        default void savePersistentProperties(Properties properties) {
            properties.setProperty(Builder.languageStrategyAffix, languageStrategyAffix());
        }


        default void syncTo(ExportsCompiler.Input.Builder builder) {
            exportDirectories().forEach(exportDirectory -> builder.addDirectoryExport(CfgStrategoConfig.exportsId, exportDirectory));
        }
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
