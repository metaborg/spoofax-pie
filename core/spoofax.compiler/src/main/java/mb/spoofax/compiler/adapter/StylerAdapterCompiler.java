package mb.spoofax.compiler.adapter;

import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.Shared;
import mb.spoofax.compiler.language.StylerLanguageCompiler;
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
public class StylerAdapterCompiler implements TaskDef<StylerAdapterCompiler.Input, StylerAdapterCompiler.Output> {
    private final TemplateWriter styleTaskDefTemplate;

    @Inject public StylerAdapterCompiler(TemplateCompiler templateCompiler) {
        templateCompiler = templateCompiler.loadingFromClass(getClass());
        this.styleTaskDefTemplate = templateCompiler.getOrCompileToWriter("styler/StyleTaskDef.java.mustache");
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public Output exec(ExecContext context, Input input) throws IOException {
        final Output.Builder outputBuilder = Output.builder();
        if(input.classKind().isManualOnly()) return outputBuilder.build(); // Nothing to generate: return.
        final ResourcePath classesGenDirectory = input.classesGenDirectory();
        styleTaskDefTemplate.write(context, input.genStyleTaskDef().file(classesGenDirectory), input);
        return outputBuilder.build();
    }


    @Value.Immutable
    public interface Input extends Serializable {
        class Builder extends StylerAdapterCompilerData.Input.Builder {}

        static Builder builder() { return new Builder(); }


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() { return ClassKind.Generated; }


        /// Adapter project classes

        @Value.Derived default ResourcePath classesGenDirectory() {
            return adapterProject().project().genSourceSpoofaxJavaDirectory();
        }

        // Style task definition

        @Value.Default default TypeInfo genStyleTaskDef() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "Style");
        }

        Optional<TypeInfo> manualStyleTaskDef();

        default TypeInfo styleTaskDef() {
            if(classKind().isManual() && manualStyleTaskDef().isPresent()) {
                return manualStyleTaskDef().get();
            }
            return genStyleTaskDef();
        }


        /// List of all generated files

        default ListView<ResourcePath> generatedFiles() {
            if(classKind().isManualOnly()) {
                return ListView.of();
            }
            return ListView.of(
                genStyleTaskDef().file(classesGenDirectory())
            );
        }


        /// Automatically provided sub-inputs

        Shared shared();

        AdapterProject adapterProject();

        StylerLanguageCompiler.Input languageProjectInput();


        @Value.Check default void check() {
            final ClassKind kind = classKind();
            final boolean manual = kind.isManualOnly();
            if(!manual) return;
            if(!manualStyleTaskDef().isPresent()) {
                throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualStyleTaskDef' has not been set");
            }
        }
    }

    @Value.Immutable
    public interface Output extends Serializable {
        class Builder extends StylerAdapterCompilerData.Output.Builder {}

        static Builder builder() { return new Builder(); }
    }
}
