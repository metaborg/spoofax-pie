package mb.spoofax.compiler.language;

import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.Conversion;
import mb.spoofax.compiler.util.GradleConfiguredDependency;
import mb.spoofax.compiler.util.Shared;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.compiler.util.TemplateWriter;
import mb.spoofax.compiler.util.TypeInfo;
import org.immutables.value.Value;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Value.Enclosing
public class StrategoRuntimeLanguageCompiler implements TaskDef<StrategoRuntimeLanguageCompiler.Input, None> {
    private final TemplateWriter factoryTemplate;

    @Inject public StrategoRuntimeLanguageCompiler(TemplateCompiler templateCompiler) {
        templateCompiler = templateCompiler.loadingFromClass(getClass());
        this.factoryTemplate = templateCompiler.getOrCompileToWriter("stratego_runtime/StrategoRuntimeBuilderFactory.java.mustache");
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public None exec(ExecContext context, Input input) throws IOException {
        if(input.classKind().isManual()) return None.instance; // Nothing to generate: return.
        final ResourcePath generatedJavaSourcesDirectory = input.generatedJavaSourcesDirectory();
        factoryTemplate.write(context, input.baseStrategoRuntimeBuilderFactory().file(generatedJavaSourcesDirectory), input);
        return None.instance;
    }

    @Override public Serializable key(Input input) {
        return input.languageProject().project().baseDirectory();
    }


    public ListView<GradleConfiguredDependency> getDependencies(Input input) {
        final Shared shared = input.shared();
        final ArrayList<GradleConfiguredDependency> dependencies = new ArrayList<>();
        dependencies.add(GradleConfiguredDependency.api(shared.strategoCommonDep()));
        dependencies.add(GradleConfiguredDependency.api(shared.orgStrategoXTStrjDep()));
        dependencies.add(GradleConfiguredDependency.api(shared.strategoXTMinJarDep()));
        if(input.addNaBL2Primitives()) {
            dependencies.add(GradleConfiguredDependency.implementation(shared.nabl2CommonDep()));
        }
        if(input.addStatixPrimitives()) {
            dependencies.add(GradleConfiguredDependency.implementation(shared.statixCommonDep()));
        }
        if(input.requiresSpoofax2Primitives()) {
            dependencies.add(GradleConfiguredDependency.implementation(shared.spoofax2CommonDep()));
        }
        if(input.requiresConstraintSolverPrimitives()) {
            dependencies.add(GradleConfiguredDependency.implementation(shared.constraintCommonDep()));
        }
        return new ListView<>(dependencies);
    }


    @Value.Immutable public interface Input extends Serializable {
        class Builder extends StrategoRuntimeLanguageCompilerData.Input.Builder {}

        static Builder builder() { return new Builder(); }


        /// Configuration

        List<String> strategyPackageIds();

        default List<String> strategyPackagePaths() {
            return strategyPackageIds().stream().map(Conversion::packageIdToPath).collect(Collectors.toList());
        }

        List<String> interopRegisterersByReflection();

        List<String> ctreeRelativePaths();

        @Value.Default default boolean addSpoofax2Primitives() { return false; }

        @Value.Default default boolean addNaBL2Primitives() { return false; }

        @Value.Default default boolean addStatixPrimitives() { return false; }

        default boolean requiresSpoofax2Primitives() {
            return addSpoofax2Primitives() || addStatixPrimitives();
        }

        default boolean requiresConstraintSolverPrimitives() {
            return addNaBL2Primitives() || addStatixPrimitives();
        }


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() { return ClassKind.Generated; }


        /// Language project classes

        default ResourcePath generatedJavaSourcesDirectory() {
            return languageProject().generatedJavaSourcesDirectory();
        }

        // Stratego runtime builder factory

        @Value.Default default TypeInfo baseStrategoRuntimeBuilderFactory() {
            return TypeInfo.of(languageProject().packageId(), shared().defaultClassPrefix() + "StrategoRuntimeBuilderFactory");
        }

        Optional<TypeInfo> extendStrategoRuntimeBuilderFactory();

        default TypeInfo strategoRuntimeBuilderFactory() {
            return extendStrategoRuntimeBuilderFactory().orElseGet(this::baseStrategoRuntimeBuilderFactory);
        }


        /// Files information, known up-front for build systems with static dependencies such as Gradle.

        default ListView<ResourcePath> javaSourceFiles() {
            if(classKind().isManual()) {
                return ListView.of();
            }
            final ResourcePath generatedJavaSourcesDirectory = generatedJavaSourcesDirectory();
            return ListView.of(
                baseStrategoRuntimeBuilderFactory().file(generatedJavaSourcesDirectory)
            );
        }


        /// Automatically provided sub-inputs

        Shared shared();

        LanguageProject languageProject();


        @Value.Check default void check() {

        }
    }
}
