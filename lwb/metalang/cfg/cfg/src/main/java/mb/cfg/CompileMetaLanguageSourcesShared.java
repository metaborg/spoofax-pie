package mb.cfg;

import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.language.LanguageProject;
import org.immutables.value.Value;

import java.io.Serializable;

@Value.Immutable
public interface CompileMetaLanguageSourcesShared extends Serializable {
    class Builder extends ImmutableCompileMetaLanguageSourcesShared.Builder {}

    static Builder builder() {
        return new Builder();
    }


    /// Directories

    @Value.Default default ResourcePath generatedResourcesDirectory() {
        return languageProject().project().buildGeneratedResourcesDirectory().appendRelativePath("metalang");
    }

    @Value.Default default ResourcePath generatedSourcesDirectory() {
        return languageProject().project().buildGeneratedSourcesDirectory().appendRelativePath("metalang");
    }

    @Value.Default default ResourcePath generatedJavaSourcesDirectory() {
        return generatedSourcesDirectory().appendRelativePath("java");
    }

    @Value.Default default ResourcePath unarchiveDirectory() {
        return languageProject().project().buildDirectory().appendRelativePath("unarchive");
    }

    @Value.Default default ResourcePath cacheDirectory() {
        return languageProject().project().buildDirectory().appendRelativePath("cache");
    }


    /// Automatically provided sub-inputs

    LanguageProject languageProject();
}
