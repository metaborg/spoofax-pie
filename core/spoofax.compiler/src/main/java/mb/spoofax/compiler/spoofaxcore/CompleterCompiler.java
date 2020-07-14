package mb.spoofax.compiler.spoofaxcore;

import mb.common.util.ListView;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.*;
import org.immutables.value.Value;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@Value.Enclosing
public class CompleterCompiler {
    private final TemplateWriter completeTaskDefTemplate;

    public CompleterCompiler(TemplateCompiler templateCompiler) {
        this.completeTaskDefTemplate = templateCompiler.getOrCompileToWriter("completer/CompleteTaskDef.java.mustache");
    }

    // Language project

    public ListView<GradleConfiguredDependency> getLanguageProjectDependencies(LanguageProjectInput input) {
        return ListView.of(
            GradleConfiguredDependency.api(input.shared().completionsCommonDep())
        );
    }

    public ListView<String> getLanguageProjectCopyResources(LanguageProjectInput input) {
        return ListView.of();
    }

    public Output compileLanguageProject(LanguageProjectInput input) throws IOException {
        final Output.Builder outputBuilder = Output.builder();
        if(input.classKind().isManualOnly()) return outputBuilder.build(); // Nothing to generate: return.
        final ResourcePath classesGenDirectory = input.classesGenDirectory();
        outputBuilder.addProvidedResources(
            //completerTemplate.write(input.genCompleter().file(classesGenDirectory), input),
            // ...
        );
        return outputBuilder.build();
    }

    // Adapter project

    public Output compileAdapterProject(AdapterProjectInput input) throws IOException {
        final Output.Builder outputBuilder = Output.builder();
        if(input.classKind().isManualOnly()) return outputBuilder.build(); // Nothing to generate: return.
        final ResourcePath classesGenDirectory = input.classesGenDirectory();
        outputBuilder.addProvidedResources(
            completeTaskDefTemplate.write(input.genCompleteTaskDef().file(classesGenDirectory), input)
        );
        return outputBuilder.build();
    }

    // Inputs

    @Value.Immutable
    public interface LanguageProjectInput extends Serializable {
        class Builder extends CompleterCompilerData.LanguageProjectInput.Builder {}

        static Builder builder() { return new Builder(); }


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() {
            return ClassKind.Generated;
        }


        /// Classes

        default ResourcePath classesGenDirectory() {
            return languageProject().project().genSourceSpoofaxJavaDirectory();
        }

        // Completer

        @Value.Default default TypeInfo genCompleter() {
            return TypeInfo.of(languageProject().packageId(), shared().defaultClassPrefix() + "Completer");
        }

        Optional<TypeInfo> manualCompleter();

        default TypeInfo completer() {
            if(classKind().isManual() && manualCompleter().isPresent()) {
                return manualCompleter().get();
            }
            return genCompleter();
        }


        /// List of all generated files

        default ListView<ResourcePath> generatedFiles() {
            if(classKind().isManualOnly()) {
                return ListView.of();
            }
            return ListView.of(
                genCompleter().file(classesGenDirectory())
            );
        }


        /// Automatically provided sub-inputs.

        Shared shared();

        LanguageProject languageProject();


        @Value.Check default void check() {
            final ClassKind kind = classKind();
            final boolean manual = kind.isManual();
            if(!manual) return;
            if(!manualCompleter().isPresent()) {
                throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualCompleter' has not been set");
            }
        }
    }

    @Value.Immutable
    public interface AdapterProjectInput extends Serializable {
        class Builder extends CompleterCompilerData.AdapterProjectInput.Builder {}

        static Builder builder() { return new Builder(); }


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() {
            return ClassKind.Generated;
        }


        /// Classes

        default ResourcePath classesGenDirectory() {
            return adapterProject().project().genSourceSpoofaxJavaDirectory();
        }

        // Complete task definition

        @Value.Default default TypeInfo genCompleteTaskDef() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "CompleteTaskDef");
        }

        Optional<TypeInfo> manualCompleteTaskDef();

        default TypeInfo completeTaskDef() {
            if(classKind().isManual() && manualCompleteTaskDef().isPresent()) {
                return manualCompleteTaskDef().get();
            }
            return genCompleteTaskDef();
        }

        // List of all generated files

        default ListView<ResourcePath> generatedFiles() {
            if(classKind().isManualOnly()) {
                return ListView.of();
            }
            return ListView.of(
                genCompleteTaskDef().file(classesGenDirectory())
            );
        }


        /// Automatically provided sub-inputs.

        Shared shared();

        AdapterProject adapterProject();

        LanguageProjectInput languageProjectInput();


        @Value.Check default void check() {
            final ClassKind kind = classKind();
            final boolean manual = kind.isManualOnly();
            if(!manual) return;
            if(!manualCompleteTaskDef().isPresent()) {
                throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualCompleteTaskDef' has not been set");
            }
        }
    }

    @Value.Immutable
    public interface Output {
        class Builder extends CompleterCompilerData.Output.Builder {}

        static Builder builder() {
            return new Builder();
        }

        List<HierarchicalResource> providedResources();
    }
}
