package mb.spoofax.compiler.language;

import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.GradleConfiguredDependency;
import mb.spoofax.compiler.util.Shared;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.compiler.util.TemplateWriter;
import mb.spoofax.compiler.util.TypeInfo;
import org.immutables.value.Value;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Optional;

@Value.Enclosing
public class ConstraintAnalyzerLanguageCompiler implements TaskDef<ConstraintAnalyzerLanguageCompiler.Input, ConstraintAnalyzerLanguageCompiler.Output> {
    private final TemplateWriter constraintAnalyzerTemplate;
    private final TemplateWriter factoryTemplate;

    @Inject public ConstraintAnalyzerLanguageCompiler(TemplateCompiler templateCompiler) {
        templateCompiler = templateCompiler.loadingFromClass(getClass());
        this.constraintAnalyzerTemplate = templateCompiler.getOrCompileToWriter("constraint_analyzer/ConstraintAnalyzer.java.mustache");
        this.factoryTemplate = templateCompiler.getOrCompileToWriter("constraint_analyzer/ConstraintAnalyzerFactory.java.mustache");
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public Output exec(ExecContext context, Input input) throws Exception {
        final Output.Builder outputBuilder = Output.builder();
        if(input.classKind().isManual()) return outputBuilder.build(); // Nothing to generate: return.
        final ResourcePath generatedJavaSourcesDirectory = input.generatedJavaSourcesDirectory();
        constraintAnalyzerTemplate.write(context, input.baseConstraintAnalyzer().file(generatedJavaSourcesDirectory), input);
        factoryTemplate.write(context, input.baseConstraintAnalyzerFactory().file(generatedJavaSourcesDirectory), input);
        return outputBuilder.build();
    }

    @Override public Serializable key(Input input) {
        return input.languageProject().project().baseDirectory();
    }


    public ListView<GradleConfiguredDependency> getDependencies(Input input) {
        return ListView.of(GradleConfiguredDependency.api(input.shared().constraintCommonDep()));
    }


    @Value.Immutable public interface Input extends Serializable {
        class Builder extends ConstraintAnalyzerLanguageCompilerData.Input.Builder {}

        static Builder builder() { return new Builder(); }


        /// Configuration

        boolean enableNaBL2();

        boolean enableStatix();

        @Value.Default default String strategoStrategy() { return "editor-analyze"; }

        @Value.Default default boolean multiFile() { return false; }


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() { return ClassKind.Generated; }


        /// Classes

        default ResourcePath generatedJavaSourcesDirectory() {
            return languageProject().generatedJavaSourcesDirectory();
        }

        // Constraint analyzer

        @Value.Default default TypeInfo baseConstraintAnalyzer() {
            return TypeInfo.of(languageProject().packageId(), shared().defaultClassPrefix() + "ConstraintAnalyzer");
        }

        Optional<TypeInfo> extendConstraintAnalyzer();

        default TypeInfo constraintAnalyzer() {
            return extendConstraintAnalyzer().orElseGet(this::baseConstraintAnalyzer);
        }

        // Constraint analyzer factory

        @Value.Default default TypeInfo baseConstraintAnalyzerFactory() {
            return TypeInfo.of(languageProject().packageId(), shared().defaultClassPrefix() + "ConstraintAnalyzerFactory");
        }

        Optional<TypeInfo> extendConstraintAnalyzerFactory();

        default TypeInfo constraintAnalyzerFactory() {
            return extendConstraintAnalyzerFactory().orElseGet(this::baseConstraintAnalyzerFactory);
        }


        /// Files information, known up-front for build systems with static dependencies such as Gradle.

        default ListView<ResourcePath> javaSourceFiles() {
            if(classKind().isManual()) {
                return ListView.of();
            }
            final ResourcePath generatedJavaSourcesDirectory = generatedJavaSourcesDirectory();
            return ListView.of(
                baseConstraintAnalyzer().file(generatedJavaSourcesDirectory),
                baseConstraintAnalyzerFactory().file(generatedJavaSourcesDirectory)
            );
        }


        /// Automatically provided sub-inputs

        Shared shared();

        LanguageProject languageProject();


        default void syncTo(StrategoRuntimeLanguageCompiler.Input.Builder builder) {
            if(enableNaBL2()) {
                builder.addNaBL2Primitives(true);
            }
            if(enableStatix()) {
                builder.addNaBL2Primitives(true);
                builder.addStatixPrimitives(true);
            }
        }


        @Value.Check default void check() {

        }
    }

    @Value.Immutable public interface Output extends Serializable {
        class Builder extends ConstraintAnalyzerLanguageCompilerData.Output.Builder {}

        static Builder builder() { return new Output.Builder(); }
    }
}
