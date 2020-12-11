package mb.spoofax.compiler.adapter;

import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.Shared;
import mb.spoofax.compiler.language.ConstraintAnalyzerLanguageCompiler;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.GradleConfiguredDependency;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.compiler.util.TemplateWriter;
import mb.spoofax.compiler.util.TypeInfo;
import org.immutables.value.Value;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Optional;

@Value.Enclosing
public class ConstraintAnalyzerAdapterCompiler implements TaskDef<ConstraintAnalyzerAdapterCompiler.Input, ConstraintAnalyzerAdapterCompiler.Output> {
    private final TemplateWriter analyzeTaskDefTemplate;
    private final TemplateWriter analyzeMultiTaskDefTemplate;

    @Inject public ConstraintAnalyzerAdapterCompiler(TemplateCompiler templateCompiler) {
        templateCompiler = templateCompiler.loadingFromClass(getClass());
        this.analyzeTaskDefTemplate = templateCompiler.getOrCompileToWriter("constraint_analyzer/AnalyzeTaskDef.java.mustache");
        this.analyzeMultiTaskDefTemplate = templateCompiler.getOrCompileToWriter("constraint_analyzer/AnalyzeMultiTaskDef.java.mustache");
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public Output exec(ExecContext context, Input input) throws Exception {
        final Output.Builder outputBuilder = Output.builder();
        if(input.classKind().isManual()) return outputBuilder.build(); // Nothing to generate: return.
        final ResourcePath generatedJavaSourcesDirectory = input.generatedJavaSourcesDirectory();
        analyzeTaskDefTemplate.write(context, input.baseAnalyzeTaskDef().file(generatedJavaSourcesDirectory), input);
        analyzeMultiTaskDefTemplate.write(context, input.baseAnalyzeMultiTaskDef().file(generatedJavaSourcesDirectory), input);
        return outputBuilder.build();
    }


    public ListView<GradleConfiguredDependency> getDependencies(Input input) {
        return ListView.of(GradleConfiguredDependency.api(input.shared().constraintPieDep()));
    }


    @Value.Immutable public interface Input extends Serializable {
        class Builder extends ConstraintAnalyzerAdapterCompilerData.Input.Builder {}

        static Builder builder() { return new Builder(); }


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() { return ClassKind.Generated; }


        /// Classes

        default ResourcePath generatedJavaSourcesDirectory() {
            return adapterProject().generatedJavaSourcesDirectory();
        }

        // Analyze

        @Value.Default default TypeInfo baseAnalyzeTaskDef() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "Analyze");
        }

        Optional<TypeInfo> extendAnalyzeTaskDef();

        default TypeInfo analyzeTaskDef() {
            return extendAnalyzeTaskDef().orElseGet(this::baseAnalyzeTaskDef);
        }

        // Multi-file analyze

        @Value.Default default TypeInfo baseAnalyzeMultiTaskDef() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "AnalyzeMulti");
        }

        Optional<TypeInfo> extendAnalyzeMultiTaskDef();

        default TypeInfo analyzeMultiTaskDef() {
            return extendAnalyzeMultiTaskDef().orElseGet(this::baseAnalyzeMultiTaskDef);
        }


        /// Files information, known up-front for build systems with static dependencies such as Gradle.

        default ListView<ResourcePath> javaSourceFiles() {
            if(classKind().isManual()) {
                return ListView.of();
            }
            final ResourcePath generatedJavaSourcesDirectory = generatedJavaSourcesDirectory();
            return ListView.of(
                baseAnalyzeTaskDef().file(generatedJavaSourcesDirectory),
                baseAnalyzeMultiTaskDef().file(generatedJavaSourcesDirectory)
            );
        }


        /// Automatically provided sub-inputs

        Shared shared();

        AdapterProject adapterProject();

        ConstraintAnalyzerLanguageCompiler.Input languageProjectInput();


        @Value.Check default void check() {

        }
    }

    @Value.Immutable public interface Output extends Serializable {
        class Builder extends ConstraintAnalyzerAdapterCompilerData.Output.Builder {}

        static Builder builder() { return new Builder(); }
    }
}
