package mb.spoofax.compiler.spoofaxcore;

import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.GradleConfiguredDependency;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.compiler.util.TemplateWriter;
import mb.spoofax.compiler.util.TypeInfo;
import org.immutables.value.Value;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Value.Enclosing
public class StrategoRuntimeLanguageCompiler implements TaskDef<StrategoRuntimeLanguageCompiler.Input, StrategoRuntimeLanguageCompiler.Output> {
    private final TemplateWriter factoryTemplate;

    @Inject public StrategoRuntimeLanguageCompiler(TemplateCompiler templateCompiler) {
        this.factoryTemplate = templateCompiler.getOrCompileToWriter("stratego_runtime/StrategoRuntimeBuilderFactory.java.mustache");
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public Output exec(ExecContext context, Input input) throws IOException {
        final Output.Builder outputBuilder = Output.builder();
        if(input.classKind().isManualOnly()) return outputBuilder.build(); // Nothing to generate: return.
        final HashMap<String, Object> map = new HashMap<>();
        map.put("addNaBL2Primitives", input.enableNaBL2() || input.enableStatix());
        map.put("addStatixPrimitives", input.enableStatix());
        final ResourcePath classesGenDirectory = input.classesGenDirectory();
        factoryTemplate.write(context, input.genFactory().file(classesGenDirectory), input, map);
        return outputBuilder.build();
    }


    public ListView<GradleConfiguredDependency> getDependencies(Input input) {
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

    public ListView<String> getCopyResources(Input input) {
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


    @Value.Immutable public interface Input extends Serializable {
        class Builder extends StrategoRuntimeLanguageCompilerData.Input.Builder {}

        static Builder builder() { return new Builder(); }


        /// Configuration

        List<String> interopRegisterersByReflection();

        boolean enableNaBL2();

        boolean enableStatix();


        /// Whether to copy certain files from the Spoofax 2.x project.

        @Value.Default default boolean copyCTree() { return false; }

        @Value.Default default boolean copyClasses() { return true; }


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() { return ClassKind.Generated; }


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

    @Value.Immutable public interface Output extends Serializable {
        class Builder extends StrategoRuntimeLanguageCompilerData.Output.Builder {}

        static Builder builder() { return new Builder(); }
    }
}
