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
import java.util.List;
import java.util.Optional;

@Value.Enclosing
public class StrategoRuntime {
    private final TemplateWriter factoryTemplate;

    public StrategoRuntime(TemplateCompiler templateCompiler) {
        this.factoryTemplate = templateCompiler.getOrCompileToWriter("stratego_runtime/StrategoRuntimeBuilderFactory.java.mustache");
    }

    // Language project

    public ListView<GradleConfiguredDependency> getLanguageProjectDependencies(Input input) {
        final Shared shared = input.shared();
        final ArrayList<GradleConfiguredDependency> dependencies = new ArrayList<>();
        dependencies.add(GradleConfiguredDependency.api(shared.strategoCommonDep()));
        dependencies.add(GradleConfiguredDependency.api(shared.orgStrategoXTStrjDep()));
        dependencies.add(GradleConfiguredDependency.api(shared.strategoXTMinJarDep()));
        // TODO: move to constraint analyzer compiler, and make this depend on it?
        // NaBL2 (required by Statix as well)
        if(input.addNaBL2Primitives() || input.addStatixPrimitives()) {
            dependencies.add(GradleConfiguredDependency.implementation(shared.nabl2CommonDep()));
        }
        if(input.addStatixPrimitives()) {
            dependencies.add(GradleConfiguredDependency.implementation(shared.statixCommonDep()));
        }
        return new ListView<>(dependencies);
    }

    public ListView<String> getLanguageProjectCopyResources(Input input) {
        final ArrayList<String> copyResources = new ArrayList<>();
        if(input.addStatixPrimitives()) {
            // TODO: move to constraint analyzer compiler?
            copyResources.add("src-gen/statix/statics.spec.aterm");
        }
        if(input.copyCTree()) {
            copyResources.add("target/metaborg/stratego.ctree");
        }
        return new ListView<>(copyResources);
    }

    public void compileLanguageProject(Input input) throws IOException {
        if(input.classKind().isManualOnly()) return; // Nothing to generate: return.
        final ResourcePath classesGenDirectory = input.languageClassesGenDirectory();
        factoryTemplate.write(input, input.genFactory().file(classesGenDirectory));
    }

    // Adapter project

    public void compileAdapterProject(Input input) throws IOException {}

    // Input

    @Value.Immutable
    public interface Input extends Serializable {
        class Builder extends StrategoRuntimeData.Input.Builder {}

        static Builder builder() {
            return new Builder();
        }


        Shared shared();


        /// Configuration

        List<String> interopRegisterersByReflection();

        boolean addNaBL2Primitives();

        boolean addStatixPrimitives();


        /// Whether to copy certain files from the Spoofax 2.x project.

        @Value.Default default boolean copyCTree() {
            return false;
        }

        @Value.Default default boolean copyClasses() {
            return true;
        }

        boolean copyJavaStrategyClasses();


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() {
            return ClassKind.Generated;
        }


        /// Language project classes

        default ResourcePath languageClassesGenDirectory() {
            return shared().languageProject().genSourceSpoofaxJavaDirectory();
        }

        // Stratego runtime builder factory

        @Value.Default default TypeInfo genFactory() {
            return TypeInfo.of(shared().languagePackage(), shared().classPrefix() + "StrategoRuntimeBuilderFactory");
        }

        Optional<TypeInfo> manualFactory();

        default TypeInfo factory() {
            if(classKind().isManual() && manualFactory().isPresent()) {
                return manualFactory().get();
            }
            return genFactory();
        }


        @Value.Check default void check() {
            final ClassKind kind = classKind();
            final boolean manual = kind.isManual();
            if(!manual) return;
            if(!manualFactory().isPresent()) {
                throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualFactory' has not been set");
            }
        }
    }
}
