package mb.spoofx.lwb.compiler.cfg;

import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.adapter.AdapterProjectCompiler;
import mb.spoofax.compiler.language.LanguageProjectCompiler;
import mb.spoofax.compiler.util.Shared;
import org.immutables.value.Value;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
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

    @Value.Default default List<ResourcePath> javaSourcePath() {
        final ArrayList<ResourcePath> additionalSourcePaths = new ArrayList<>();
        additionalSourcePaths.add(adapterProjectInput().adapterProject().project().srcMainDirectory().appendSegment("java"));
        return additionalSourcePaths;
    }

    List<File> javaClassPath();

    List<File> javaAnnotationProcessorPath();

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
