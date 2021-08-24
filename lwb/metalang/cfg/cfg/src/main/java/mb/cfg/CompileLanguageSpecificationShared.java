package mb.cfg;

import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.language.LanguageProject;
import org.immutables.value.Value;

import java.io.Serializable;

@Value.Immutable
public interface CompileLanguageSpecificationShared extends Serializable {
    class Builder extends ImmutableCompileLanguageSpecificationShared.Builder {}

    static Builder builder() {
        return new Builder();
    }


    /// Configuration

    @Value.Default default boolean includeLibSpoofax2Exports() {
        return true;
    }

    @Value.Default default boolean includeLibStatixExports() {
        return true;
    }


    /// Directories

    @Value.Default default ResourcePath generatedResourcesDirectory() {
        return languageProject().project().buildGeneratedResourcesDirectory().appendRelativePath("languageSpecification");
    }

    @Value.Default default ResourcePath generatedSourcesDirectory() {
        return languageProject().project().buildGeneratedSourcesDirectory().appendRelativePath("languageSpecification");
    }

    @Value.Default default ResourcePath generatedJavaSourcesDirectory() {
        return generatedSourcesDirectory().appendRelativePath("java");
    }

    @Value.Default default ResourcePath generatedStatixSourcesDirectory() {
        return generatedSourcesDirectory().appendRelativePath("statix");
    }

    @Value.Default default ResourcePath generatedStrategoSourcesDirectory() {
        return generatedSourcesDirectory().appendRelativePath("stratego");
    }

    @Value.Default default ResourcePath unarchiveDirectory() {
        return languageProject().project().buildDirectory().appendRelativePath("unarchive");
    }


    /// Automatically provided sub-inputs

    LanguageProject languageProject();
}