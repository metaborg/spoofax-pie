package mb.spoofax.compiler.spoofaxcore;

import mb.common.util.ListView;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.GradleConfiguredDependency;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.compiler.util.TemplateWriter;
import mb.spoofax.compiler.util.TypeInfo;
import org.immutables.value.Value;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@Value.Enclosing
public class ClassloaderResourcesCompiler {
    private final TemplateWriter classloaderResourcesTemplate;

    public ClassloaderResourcesCompiler(TemplateCompiler templateCompiler) {
        this.classloaderResourcesTemplate = templateCompiler.getOrCompileToWriter("classloader_resources/ClassloaderResources.java.mustache");
    }

    // Language project

    public ListView<GradleConfiguredDependency> getLanguageProjectDependencies(LanguageProjectInput input) {
        return ListView.of();
    }

    public ListView<String> getLanguageProjectCopyResources(LanguageProjectInput input) {
        return ListView.of();
    }

    public Output compileLanguageProject(LanguageProjectInput input) throws IOException {
        final Output.Builder outputBuilder = Output.builder();
        if(input.classKind().isManualOnly()) return outputBuilder.build(); // Nothing to generate: return.
        final ResourcePath classesGenDirectory = input.classesGenDirectory();
        outputBuilder.addProvidedResources(
            classloaderResourcesTemplate.write(input.genClassloaderResources().file(classesGenDirectory), input)
        );
        return outputBuilder.build();
    }

    // Adapter project

    public Output compileAdapterProject(AdapterProjectInput input) throws IOException {
        // Nothing to generate for adapter project at the moment.
        return Output.builder().build();
    }

    // Inputs & outputs

    @Value.Immutable
    public interface LanguageProjectInput extends Serializable {
        class Builder extends ClassloaderResourcesCompilerData.LanguageProjectInput.Builder {}

        static Builder builder() {
            return new Builder();
        }


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() {
            return ClassKind.Generated;
        }


        /// Language project classes

        default ResourcePath classesGenDirectory() {
            return languageProject().project().genSourceSpoofaxJavaDirectory();
        }

        // Classloader resources

        @Value.Default default TypeInfo genClassloaderResources() {
            return TypeInfo.of(languageProject().packageId(), shared().defaultClassPrefix() + "ClassloaderResources");
        }

        Optional<TypeInfo> manualClassloaderResources();

        default TypeInfo classloaderResources() {
            if(classKind().isManual() && manualClassloaderResources().isPresent()) {
                return manualClassloaderResources().get();
            }
            return genClassloaderResources();
        }

        @Value.Default default String qualifier() {
            return shared().defaultBasePackageId().replace(".", "-") + "-classloader-resource";
        }

        /// List of all provided files

        default ListView<ResourcePath> providedFiles() {
            if(classKind().isManualOnly()) {
                return ListView.of();
            }
            return ListView.of(
                genClassloaderResources().file(classesGenDirectory())
            );
        }


        /// Automatically provided sub-inputs

        Shared shared();

        LanguageProject languageProject();


        @Value.Check default void check() {
            final ClassKind kind = classKind();
            final boolean manual = kind.isManual();
            if(!manual) return;
            if(!manualClassloaderResources().isPresent()) {
                throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualClassloaderResources' has not been set");
            }
        }
    }

    @Value.Immutable
    public interface AdapterProjectInput extends Serializable {
        class Builder extends ClassloaderResourcesCompilerData.AdapterProjectInput.Builder {}

        static Builder builder() {
            return new Builder();
        }


        /// Automatically provided sub-inputs

        LanguageProjectInput languageProjectInput();
    }

    @Value.Immutable
    public interface Output {
        class Builder extends ClassloaderResourcesCompilerData.Output.Builder {}

        static Builder builder() {
            return new Builder();
        }

        List<HierarchicalResource> providedResources();
    }
}
