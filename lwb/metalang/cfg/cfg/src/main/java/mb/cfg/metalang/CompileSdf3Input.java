package mb.cfg.metalang;

import mb.cfg.CompileLanguageShared;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.language.ParserLanguageCompiler;
import mb.spoofax.compiler.util.BuilderBase;
import mb.spoofax.compiler.util.Conversion;
import mb.spoofax.compiler.util.Shared;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.Properties;

@Value.Immutable
public interface CompileSdf3Input extends Serializable {
    class Builder extends ImmutableCompileSdf3Input.Builder implements BuilderBase {
        static final String propertiesPrefix = "sdf3.";
        static final String sdf3StrategoStrategyIdAffix = propertiesPrefix + "strategoStrategyIdAffix";

        public Builder withPersistentProperties(Properties properties) {
            with(properties, sdf3StrategoStrategyIdAffix, this::strategoStrategyIdAffix);
            return this;
        }
    }

    static Builder builder() { return new Builder(); }


    default ResourcePath rootDirectory() {
        return compileLanguageShared().languageProject().project().baseDirectory();
    }

    @Value.Default default ResourcePath mainSourceDirectory() {
        return rootDirectory().appendRelativePath("src");
    }

    @Value.Default default ResourcePath mainFile() {
        return mainSourceDirectory().appendRelativePath("start.sdf3");
    }


    @Value.Default default boolean createDynamicParseTable() {
        return false;
    }

    @Value.Default default boolean createDataDependentParseTable() {
        return false;
    }

    @Value.Default default boolean createLayoutSensitiveParseTable() {
        return false;
    }

    @Value.Default default boolean solveDeepConflictsInParseTable() {
        return true;
    }

    @Value.Default default boolean checkOverlapInParseTable() {
        return false;
    }

    @Value.Default default boolean checkPrioritiesInParseTable() {
        return false;
    }


    @Value.Default default String strategoStrategyIdAffix() {
        // TODO: convert to Stratego ID instead of Java ID.
        return Conversion.nameToJavaId(shared().name().toLowerCase());
    }

    @Value.Default default String parseTableRelativePath() {
        return "sdf.tbl";
    }

    default ResourcePath parseTableOutputFile() {
        return compileLanguageShared().generatedResourcesDirectory() // Generated resources directory, so that Gradle includes the parse table in the JAR file.
            .appendRelativePath(compileLanguageShared().languageProject().packagePath()) // Append package path to make location unique, enabling JAR files to be merged.
            .appendRelativePath(parseTableRelativePath()) // Append the relative path to the parse table.
            ;
    }


    /// Automatically provided sub-inputs

    @Value.Auxiliary Shared shared();

    CompileLanguageShared compileLanguageShared();


    default void savePersistentProperties(Properties properties) {
        properties.setProperty(Builder.sdf3StrategoStrategyIdAffix, strategoStrategyIdAffix());
    }


    default void syncTo(ParserLanguageCompiler.Input.Builder builder) {
        builder.parseTableRelativePath(parseTableRelativePath());
    }

    default void syncTo(CompileStrategoInput.Builder builder) {
        builder.addStrategoIncludeDirs(compileLanguageShared().generatedStrategoSourcesDirectory());
    }
}
