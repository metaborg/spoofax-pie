package mb.cfg.metalang;

import mb.cfg.CompileLanguageSpecificationShared;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.language.ParserLanguageCompiler;
import org.immutables.value.Value;

import java.io.Serializable;

@Value.Immutable
public interface CompileSdf3Input extends Serializable {
    class Builder extends ImmutableCompileSdf3Input.Builder {}

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

    CompileLanguageSpecificationShared compileLanguageShared();


    default void syncTo(ParserLanguageCompiler.Input.Builder builder) {
        builder.parseTableRelativePath(parseTableRelativePath());
    }
}
