package mb.spoofax.compiler.spoofax3.language;

import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.language.LanguageProject;
import org.immutables.value.Value;

import java.io.Serializable;

@Value.Immutable
public interface Spoofax3LanguageProject extends Serializable {
    class Builder extends ImmutableSpoofax3LanguageProject.Builder {}

    static Builder builder() {
        return new Builder();
    }


    /// Configuration

    @Value.Default default boolean includeLibSpoofax2Exports() {
        return true;
    }


    /// Directories

    @Value.Default default ResourcePath generatedResourcesDirectory() {
        return languageProject().project().buildGeneratedResourcesDirectory().appendRelativePath("spoofax3Language");
    }

    @Value.Default default ResourcePath generatedSourcesDirectory() {
        return languageProject().project().buildGeneratedSourcesDirectory().appendRelativePath("spoofax3Language");
    }

    @Value.Default default ResourcePath generatedJavaSourcesDirectory() {
        return generatedSourcesDirectory().appendRelativePath("java");
    }

    @Value.Default default ResourcePath generatedStrategoSourcesDirectory() {
        return generatedSourcesDirectory().appendRelativePath("stratego");
    }


    /// Automatically provided sub-inputs

    LanguageProject languageProject();
}
