package mb.spoofax.compiler.spoofaxcore;

import mb.common.util.ListView;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.GradleConfiguredDependency;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.compiler.util.TemplateWriter;
import mb.spoofax.compiler.util.TypeInfo;
import org.immutables.value.Value;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Value.Enclosing
public class StrategoRuntimeCompiler {
    private final TemplateWriter factoryTemplate;

    public StrategoRuntimeCompiler(TemplateCompiler templateCompiler) {
        this.factoryTemplate = templateCompiler.getOrCompileToWriter("stratego_runtime/StrategoRuntimeBuilderFactory.java.mustache");
    }

    // Language project

    public ListView<GradleConfiguredDependency> getLanguageProjectDependencies(LanguageProjectInput input) {
        final Shared shared = input.shared();
        final ArrayList<GradleConfiguredDependency> dependencies = new ArrayList<>();
        dependencies.add(GradleConfiguredDependency.api(shared.strategoCommonDep()));
        dependencies.add(GradleConfiguredDependency.api(shared.orgStrategoXTStrjDep()));
        dependencies.add(GradleConfiguredDependency.api(shared.strategoXTMinJarDep()));
        // TODO: move to constraint analyzer compiler, and make this depend on it?
        // NaBL2 (required by Statix as well)
        if(input.enableNaBL2() || input.enableStatix()) {
            dependencies.add(GradleConfiguredDependency.implementation(shared.nabl2CommonDep()));
        }
        if(input.enableStatix()) {
            dependencies.add(GradleConfiguredDependency.implementation(shared.statixCommonDep()));
            dependencies.add(GradleConfiguredDependency.implementation(shared.spoofax2CommonDep()));
        }
        return new ListView<>(dependencies);
    }

    public ListView<String> getLanguageProjectCopyResources(LanguageProjectInput input) {
        final ArrayList<String> copyResources = new ArrayList<>();
        if(input.enableStatix()) {
            // TODO: move to constraint analyzer compiler?
            copyResources.add("src-gen/statix/");
        }
        if(input.copyCTree()) {
            copyResources.add("target/metaborg/stratego.ctree");
        }
        return new ListView<>(copyResources);
    }

    public Output compileLanguageProject(LanguageProjectInput input) throws IOException {
        final Output.Builder outputBuilder = Output.builder();
        if(input.classKind().isManualOnly()) return outputBuilder.build(); // Nothing to generate: return.

        final HashMap<String, Object> map = new HashMap<>();
        map.put("addNaBL2Primitives", input.enableNaBL2() || input.enableStatix());
        map.put("addStatixPrimitives", input.enableStatix());
        final ResourcePath classesGenDirectory = input.classesGenDirectory();
        factoryTemplate.write(input.genFactory().file(classesGenDirectory), input, map);

        outputBuilder.addAllProvidedFiles(input.providedFiles());
        return outputBuilder.build();
    }

    // Adapter project

    public ListView<GradleConfiguredDependency> getAdapterProjectDependencies(AdapterProjectInput input) {
        return ListView.of(
            GradleConfiguredDependency.api(input.languageProjectInput().shared().strategoPieDep())
        );
    }

    public Output compileAdapterProject(AdapterProjectInput input) throws IOException {
        // Nothing to generate for adapter project at the moment.
        return Output.builder().build();
    }

    // Inputs & outputs

    @Value.Immutable
    public interface LanguageProjectInput extends Serializable {
        class Builder extends StrategoRuntimeCompilerData.LanguageProjectInput.Builder {}

        static Builder builder() {
            return new Builder();
        }


        /// Configuration

        List<String> interopRegisterersByReflection();

        boolean enableNaBL2();

        boolean enableStatix();


        /// Whether to copy certain files from the Spoofax 2.x project.

        @Value.Default default boolean copyCTree() {
            return false;
        }

        @Value.Default default boolean copyClasses() {
            return true;
        }


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() {
            return ClassKind.Generated;
        }


        /// Language project classes

        default ResourcePath classesGenDirectory() {
            return languageProject().project().genSourceSpoofaxJavaDirectory();
        }

        // Stratego runtime builder factory

        @Value.Default default TypeInfo genFactory() {
            return TypeInfo.of(languageProject().packageId(), shared().defaultClassPrefix() + "StrategoRuntimeBuilderFactory");
        }

        Optional<TypeInfo> manualFactory();

        default TypeInfo factory() {
            if(classKind().isManual() && manualFactory().isPresent()) {
                return manualFactory().get();
            }
            return genFactory();
        }


        // List of all provided files

        default ListView<ResourcePath> providedFiles() {
            if(classKind().isManualOnly()) {
                return ListView.of();
            }
            return ListView.of(
                genFactory().file(classesGenDirectory())
            );
        }


        /// Automatically provided sub-inputs

        Shared shared();

        LanguageProject languageProject();


        @Value.Check default void check() {
            final ClassKind kind = classKind();
            final boolean manual = kind.isManualOnly();
            if(!manual) return;
            if(!manualFactory().isPresent()) {
                throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualFactory' has not been set");
            }
        }
    }

    @Value.Immutable
    public interface AdapterProjectInput extends Serializable {
        class Builder extends StrategoRuntimeCompilerData.AdapterProjectInput.Builder {}

        static Builder builder() {
            return new Builder();
        }


        /// Automatically provided sub-inputs

        LanguageProjectInput languageProjectInput();
    }

    @Value.Immutable
    public interface Output {
        class Builder extends StrategoRuntimeCompilerData.Output.Builder {}

        static Builder builder() {
            return new Builder();
        }

        List<ResourcePath> providedFiles();
    }
}
