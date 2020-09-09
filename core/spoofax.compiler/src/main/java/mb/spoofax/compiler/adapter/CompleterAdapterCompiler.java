package mb.spoofax.compiler.adapter;

import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.Shared;
import mb.spoofax.compiler.language.CompleterLanguageCompiler;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.compiler.util.TemplateWriter;
import mb.spoofax.compiler.util.TypeInfo;
import org.immutables.value.Value;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.Optional;

@Value.Enclosing
public class CompleterAdapterCompiler implements TaskDef<CompleterAdapterCompiler.Input, CompleterAdapterCompiler.Output> {
    private final TemplateWriter completeTaskDefTemplate;

    @Inject public CompleterAdapterCompiler(TemplateCompiler templateCompiler) {
        templateCompiler = templateCompiler.loadingFromClass(getClass());
        this.completeTaskDefTemplate = templateCompiler.getOrCompileToWriter("completer/CompleteTaskDef.java.mustache");
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public Output exec(ExecContext context, Input input) throws IOException {
        final Output.Builder outputBuilder = Output.builder();
        if(input.classKind().isManualOnly()) return outputBuilder.build(); // Nothing to generate: return.
        final ResourcePath classesGenDirectory = input.classesGenDirectory();
        completeTaskDefTemplate.write(context, input.genCompleteTaskDef().file(classesGenDirectory), input);
        return outputBuilder.build();
    }


    @Value.Immutable public interface Input extends Serializable {
        class Builder extends CompleterAdapterCompilerData.Input.Builder {}

        static Builder builder() { return new Builder(); }


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() { return ClassKind.Generated; }


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

        CompleterLanguageCompiler.Input languageProjectInput();


        @Value.Check default void check() {
            final ClassKind kind = classKind();
            final boolean manual = kind.isManualOnly();
            if(!manual) return;
            if(!manualCompleteTaskDef().isPresent()) {
                throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualCompleteTaskDef' has not been set");
            }
        }
    }

    @Value.Immutable public interface Output extends Serializable {
        class Builder extends CompleterAdapterCompilerData.Output.Builder {}

        static Builder builder() { return new Builder(); }
    }
}
