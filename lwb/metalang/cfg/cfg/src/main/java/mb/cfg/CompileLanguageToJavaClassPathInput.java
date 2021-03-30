package mb.cfg;

import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.adapter.AdapterProjectCompiler;
import mb.spoofax.compiler.language.LanguageProjectCompiler;
import mb.spoofax.compiler.util.Shared;
import org.immutables.value.Value;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;

@Value.Immutable
public interface CompileLanguageToJavaClassPathInput extends Serializable {
    class Builder extends ImmutableCompileLanguageToJavaClassPathInput.Builder {}

    static Builder builder() {
        return new Builder();
    }


    /// Sub-inputs

    Shared shared();

    LanguageProjectCompiler.Input languageProjectInput();

    CompileLanguageInput compileLanguageInput();

    AdapterProjectCompiler.Input adapterProjectInput();


    /// Java compilation

    default LinkedHashSet<ResourcePath> javaSourceFiles() { // LinkedHashSet to preserve insertion order.
        final LinkedHashSet<ResourcePath> javaSourceFiles = new LinkedHashSet<>();
        javaSourceFiles.addAll(languageProjectInput().javaSourceFiles());
        javaSourceFiles.addAll(compileLanguageInput().javaSourceFiles());
        javaSourceFiles.addAll(adapterProjectInput().javaSourceFiles());
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
        javaSourcePath.addAll(compileLanguageInput().javaSourcePaths());
        javaSourcePath.addAll(adapterProjectInput().javaSourcePaths());
        return javaSourcePath;
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


    default void savePersistentProperties(Properties properties) {
        shared().savePersistentProperties(properties);
        compileLanguageInput().savePersistentProperties(properties);
    }
}
