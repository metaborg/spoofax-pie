package mb.spoofax.compiler.adapter;

import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.language.ClassLoaderResourcesCompiler;
import mb.spoofax.compiler.language.StrategoRuntimeLanguageCompiler;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.GradleConfiguredDependency;
import mb.spoofax.compiler.util.NamedTypeInfo;
import mb.spoofax.compiler.util.Shared;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.compiler.util.TemplateWriter;
import mb.spoofax.compiler.util.TypeInfo;
import org.immutables.value.Value;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Value.Enclosing
public class StrategoRuntimeAdapterCompiler {
    private final TemplateWriter getStrategoRuntimeTaskDefTemplate;

    @Inject public StrategoRuntimeAdapterCompiler(TemplateCompiler templateCompiler) {
        templateCompiler = templateCompiler.loadingFromClass(getClass());
        this.getStrategoRuntimeTaskDefTemplate = templateCompiler.getOrCompileToWriter("stratego_runtime/GetStrategoRuntimeProvider.java.mustache");
    }


    public None compile(ExecContext context, Input input) throws IOException {
        if(input.classKind().isManual()) return None.instance; // Nothing to generate: return.
        final ResourcePath generatedJavaSourcesDirectory = input.generatedJavaSourcesDirectory();
        getStrategoRuntimeTaskDefTemplate.write(context, input.baseGetStrategoRuntimeProviderTaskDef().file(generatedJavaSourcesDirectory), input);
        return None.instance;
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

        // Additional arguments to be injected in extended Stratego Runtime Builder Factory

        @Value.Default default List<TypeInfo> extendStrategoRuntimeBuilderFactoryCustomArgs() {
            return Collections.emptyList();
        }

        @Value.Lazy default List<NamedTypeInfo> extendStrategoRuntimeBuilderFactoryCustomVars() {
            ArrayList<NamedTypeInfo> results = new ArrayList<>();
            for (int i = 0; i < extendStrategoRuntimeBuilderFactoryCustomArgs().size(); i++) {
                results.add(NamedTypeInfo.of("custom" + i, extendStrategoRuntimeBuilderFactoryCustomArgs().get(i)));
            }
            return results;
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

        @Value.Auxiliary Shared shared();

        AdapterProject adapterProject();

        StrategoRuntimeLanguageCompiler.Input languageProjectInput();

        ClassLoaderResourcesCompiler.Input classLoaderResourcesInput();
    }
}
