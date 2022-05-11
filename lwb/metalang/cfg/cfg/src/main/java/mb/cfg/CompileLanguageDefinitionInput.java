package mb.cfg;

import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.adapter.AdapterProjectCompiler;
import mb.spoofax.compiler.language.LanguageProjectCompiler;
import mb.spoofax.compiler.platform.EclipseProjectCompiler;
import mb.spoofax.compiler.util.Shared;
import org.immutables.value.Value;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

@Value.Immutable
public interface CompileLanguageDefinitionInput extends Serializable {
    class Builder extends ImmutableCompileLanguageDefinitionInput.Builder {}

    static Builder builder() {
        return new Builder();
    }


    /// Sub-inputs

    Shared shared();

    LanguageProjectCompiler.Input languageProjectInput();

    CompileMetaLanguageSourcesInput compileMetaLanguageSourcesInput();

    AdapterProjectCompiler.Input adapterProjectInput();

    Optional<EclipseProjectCompiler.Input> eclipseProjectInput();


    /// Java compilation

    default LinkedHashSet<ResourcePath> javaSourceFiles() { // LinkedHashSet to preserve insertion order.
        final LinkedHashSet<ResourcePath> javaSourceFiles = new LinkedHashSet<>();
        javaSourceFiles.addAll(languageProjectInput().javaSourceFiles());
        javaSourceFiles.addAll(compileMetaLanguageSourcesInput().javaSourceFiles());
        javaSourceFiles.addAll(adapterProjectInput().javaSourceFiles());
        eclipseProjectInput().ifPresent(i -> javaSourceFiles.addAll(i.javaSourceFiles()));
        return javaSourceFiles;
    }

    @Value.Default default List<ResourcePath> userJavaSourcePaths() {
        final ArrayList<ResourcePath> sourcePaths = new ArrayList<>();
        sourcePaths.add(adapterProjectInput().adapterProject().project().srcMainDirectory().appendSegment("java"));
        return sourcePaths;
    }

    default LinkedHashSet<ResourcePath> javaSourcePaths() { // LinkedHashSet to preserve insertion order.
        final LinkedHashSet<ResourcePath> javaSourcePath = new LinkedHashSet<>(userJavaSourcePaths());
        javaSourcePath.addAll(languageProjectInput().javaSourcePaths());
        javaSourcePath.addAll(compileMetaLanguageSourcesInput().javaSourcePaths());
        javaSourcePath.addAll(adapterProjectInput().javaSourcePaths());
        eclipseProjectInput().ifPresent(i -> javaSourcePath.addAll(i.javaSourcePaths()));
        return javaSourcePath;
    }

    default LinkedHashSet<ResourcePath> javaSourceDirectoryPaths() { // LinkedHashSet to preserve insertion order.
        final LinkedHashSet<ResourcePath> javaSourceDirectoryPath = new LinkedHashSet<>();
        javaSourceDirectoryPath.addAll(compileMetaLanguageSourcesInput().javaSourceDirectoryPaths());
        return javaSourceDirectoryPath;
    }

    List<File> javaClassPaths();

    List<File> javaAnnotationProcessorPaths();

    @Value.Default default String javaRelease() {
        return "8";
    }

    @Value.Default default ResourcePath javaSourceFileOutputDirectory() {
        return adapterProjectInput().adapterProject().project().buildGeneratedSourcesAnnotationProcessorJavaMainDirectory();
    }

    @Value.Default default ResourcePath javaClassFileOutputDirectory() {
        return adapterProjectInput().adapterProject().project().buildClassesJavaMainDirectory();
    }


    List<ResourcePath> userResourcePaths();

    default LinkedHashSet<ResourcePath> resourcePaths() { // LinkedHashSet to preserve insertion order.
        final LinkedHashSet<ResourcePath> resourcePaths = new LinkedHashSet<>(userResourcePaths());
        resourcePaths.addAll(compileMetaLanguageSourcesInput().resourcePaths());
        eclipseProjectInput().ifPresent(i -> resourcePaths.addAll(i.resourcePaths()));
        return resourcePaths;
    }


    default void savePersistentProperties(Properties properties) {
        shared().savePersistentProperties(properties);
        compileMetaLanguageSourcesInput().savePersistentProperties(properties);
    }
}
