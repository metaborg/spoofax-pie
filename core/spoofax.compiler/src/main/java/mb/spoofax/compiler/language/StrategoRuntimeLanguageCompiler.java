package mb.spoofax.compiler.language;

import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.Conversion;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Value.Enclosing
public class StrategoRuntimeLanguageCompiler {
    private final TemplateWriter factoryTemplate;

    @Inject public StrategoRuntimeLanguageCompiler(TemplateCompiler templateCompiler) {
        templateCompiler = templateCompiler.loadingFromClass(getClass());
        this.factoryTemplate = templateCompiler.getOrCompileToWriter("stratego_runtime/StrategoRuntimeBuilderFactory.java.mustache");
    }


    public None compile(ExecContext context, Input input) throws IOException {
        if(input.classKind().isManual()) return None.instance; // Nothing to generate: return.
        final ResourcePath generatedJavaSourcesDirectory = input.generatedJavaSourcesDirectory();
        factoryTemplate.write(context, input.baseStrategoRuntimeBuilderFactory().file(generatedJavaSourcesDirectory), input);
        return None.instance;
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

        /**
         * A list of fully-qualified class names of Stratego primitive libraries implementing the
         * {@code org.spoofax.interpreter.library.IOperatorRegistry} interface.
         * Each class should have one constructor with the {@link Inject} annotation,
         * usually a parameterless constructor.
         * <p>
         * The libraries are registered using {@code mb.stratego.common.StrategoRuntimeBuilder#addLibrary}
         * in the generated {@code StrategoRuntimeBuilderFactory} class.
         */
        List<TypeInfo> primitiveLibraries();

        /**
         * Computes a list of named variables for the primitive libraries.
         * @return a list of named variables
         */
        @Value.Lazy default List<NamedTypeInfo> primitiveLibraryVars() {
            ArrayList<NamedTypeInfo> results = new ArrayList<>();
            for (int i = 0; i < primitiveLibraries().size(); i++) {
                results.add(NamedTypeInfo.of("primitiveLibrary" + i, primitiveLibraries().get(i)));
            }
            return results;
        }

        /**
         * A list of fully-qualified class names of Stratego interop libraries implementing the
         * {@code org.strategoxt.lang.InteropRegisterer} interface.
         * Each class should have one constructor with the {@link Inject} annotation,
         * usually a parameterless constructor.
         * <p>
         * The libraries are registered using {@code mb.stratego.common.StrategoRuntimeBuilder#addInteropRegisterer}
         * in the generated {@code StrategoRuntimeBuilderFactory} class.
         */
        List<TypeInfo> interopRegisterers();

        /**
         * Computes a list of named variables for the interop registerers.
         * @return a list of named variables
         */
        @Value.Lazy default List<NamedTypeInfo> interopRegistererVars() {
            ArrayList<NamedTypeInfo> results = new ArrayList<>();
            for (int i = 0; i < interopRegisterers().size(); i++) {
                results.add(NamedTypeInfo.of("interopRegisterer" + i, interopRegisterers().get(i)));
            }
            return results;
        }

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
