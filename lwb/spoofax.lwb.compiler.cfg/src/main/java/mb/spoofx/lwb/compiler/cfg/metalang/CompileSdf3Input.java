package mb.spoofx.lwb.compiler.cfg.metalang;

import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.language.ParserLanguageCompiler;
import mb.spoofax.compiler.util.BuilderBase;
import mb.spoofax.compiler.util.Conversion;
import mb.spoofax.compiler.util.Shared;
import mb.spoofx.lwb.compiler.cfg.CompileLanguageShared;
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

    @Value.Default default ResourcePath sourceDirectory() {
        return compileLanguageShared().languageProject().project().srcDirectory();
    }

    @Value.Default default ResourcePath mainFile() {
        return sourceDirectory().appendRelativePath("start.sdf3");
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

    @Value.Default default String sdf3ParseTableRelativePath() {
        return "sdf.tbl";
    }

    default ResourcePath sdf3ParseTableOutputFile() {
        return compileLanguageShared().generatedResourcesDirectory() // Generated resources directory, so that Gradle includes the parse table in the JAR file.
            .appendRelativePath(compileLanguageShared().languageProject().packagePath()) // Append package path to make location unique, enabling JAR files to be merged.
            .appendRelativePath(sdf3ParseTableRelativePath()) // Append the relative path to the parse table.
            ;
    }


    /// Automatically provided sub-inputs

    @Value.Auxiliary Shared shared();

    CompileLanguageShared compileLanguageShared();


    default void savePersistentProperties(Properties properties) {
        properties.setProperty(Builder.sdf3StrategoStrategyIdAffix, strategoStrategyIdAffix());
    }


    default void syncTo(ParserLanguageCompiler.Input.Builder builder) {
        builder.parseTableRelativePath(sdf3ParseTableRelativePath());
    }

    default void syncTo(CompileStrategoInput.Builder builder) {
        builder.addStrategoIncludeDirs(compileLanguageShared().generatedStrategoSourcesDirectory());
    }
}
