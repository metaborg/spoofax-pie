package mb.spoofax.compiler.adapter;

import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.language.ClassLoaderResourcesCompiler;
import mb.spoofax.compiler.language.StrategoRuntimeLanguageCompiler;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.GradleConfiguredDependency;
import mb.spoofax.compiler.util.Shared;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.compiler.util.TemplateWriter;
import mb.spoofax.compiler.util.TypeInfo;
import org.immutables.value.Value;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.Optional;

@Value.Enclosing
public class StrategoRuntimeAdapterCompiler implements TaskDef<StrategoRuntimeAdapterCompiler.Input, StrategoRuntimeAdapterCompiler.Output> {
    private final TemplateWriter getStrategoRuntimeTaskDefTemplate;

    @Inject public StrategoRuntimeAdapterCompiler(TemplateCompiler templateCompiler) {
        templateCompiler = templateCompiler.loadingFromClass(getClass());
        this.getStrategoRuntimeTaskDefTemplate = templateCompiler.getOrCompileToWriter("stratego_runtime/GetStrategoRuntimeProvider.java.mustache");
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public Output exec(ExecContext context, Input input) throws IOException {
        final Output.Builder outputBuilder = Output.builder();
        if(input.classKind().isManual()) return outputBuilder.build(); // Nothing to generate: return.
        final ResourcePath generatedJavaSourcesDirectory = input.generatedJavaSourcesDirectory();
        getStrategoRuntimeTaskDefTemplate.write(context, input.baseGetStrategoRuntimeProviderTaskDef().file(generatedJavaSourcesDirectory), input);
        return outputBuilder.build();
    }


    public ListView<GradleConfiguredDependency> getDependencies(Input input) {
        return ListView.of(
            GradleConfiguredDependency.api(input.languageProjectInput().shared().strategoPieDep())
        );
    }


    @Value.Immutable public interface Input extends Serializable {
        class Builder extends StrategoRuntimeAdapterCompilerData.Input.Builder {}

        static Builder builder() { return new Builder(); }


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() { return ClassKind.Generated; }


        /// Adapter project classes

        default ResourcePath generatedJavaSourcesDirectory() {
            return adapterProject().generatedJavaSourcesDirectory();
        }

        // Style task definition

        @Value.Default default TypeInfo baseGetStrategoRuntimeProviderTaskDef() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "GetStrategoRuntimeProvider");
        }

        Optional<TypeInfo> extendGetStrategoRuntimeProviderTaskDef();

        default TypeInfo getStrategoRuntimeProviderTaskDef() {
            return extendGetStrategoRuntimeProviderTaskDef().orElseGet(this::baseGetStrategoRuntimeProviderTaskDef);
        }


        /// Files information, known up-front for build systems with static dependencies such as Gradle.

        default ListView<ResourcePath> javaSourceFiles() {
            if(classKind().isManual()) {
                return ListView.of();
            }
            final ResourcePath generatedJavaSourcesDirectory = generatedJavaSourcesDirectory();
            return ListView.of(
                baseGetStrategoRuntimeProviderTaskDef().file(generatedJavaSourcesDirectory)
            );
        }


        /// Automatically provided sub-inputs

        Shared shared();

        AdapterProject adapterProject();

        StrategoRuntimeLanguageCompiler.Input languageProjectInput();

        ClassLoaderResourcesCompiler.Input classLoaderResourcesInput();
    }

    @Value.Immutable public interface Output extends Serializable {
        class Builder extends StrategoRuntimeAdapterCompilerData.Output.Builder {}

        static Builder builder() { return new Builder(); }
    }
}
