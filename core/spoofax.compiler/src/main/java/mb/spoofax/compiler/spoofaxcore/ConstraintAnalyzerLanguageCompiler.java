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
import java.io.Serializable;
import java.util.Optional;

@Value.Enclosing
public class ConstraintAnalyzerLanguageCompiler implements TaskDef<ConstraintAnalyzerLanguageCompiler.Input, ConstraintAnalyzerLanguageCompiler.Output> {
    private final TemplateWriter constraintAnalyzerTemplate;
    private final TemplateWriter factoryTemplate;

    @Inject public ConstraintAnalyzerLanguageCompiler(TemplateCompiler templateCompiler) {
        this.constraintAnalyzerTemplate = templateCompiler.getOrCompileToWriter("constraint_analyzer/ConstraintAnalyzer.java.mustache");
        this.factoryTemplate = templateCompiler.getOrCompileToWriter("constraint_analyzer/ConstraintAnalyzerFactory.java.mustache");
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public Output exec(ExecContext context, Input input) throws Exception {
        final Output.Builder outputBuilder = Output.builder();
        if(input.classKind().isManualOnly()) return outputBuilder.build(); // Nothing to generate: return.
        final ResourcePath classesGenDirectory = input.classesGenDirectory();
        constraintAnalyzerTemplate.write(context, input.genConstraintAnalyzer().file(classesGenDirectory), input);
        factoryTemplate.write(context, input.genFactory().file(classesGenDirectory), input);
        return outputBuilder.build();
    }


    public ListView<GradleConfiguredDependency> getDependencies(Input input) {
        return ListView.of(GradleConfiguredDependency.api(input.shared().constraintCommonDep()));
    }

    public ListView<String> getCopyResources(Input input) {
        return ListView.of();
    }


    @Value.Immutable public interface Input extends Serializable {
        class Builder extends ConstraintAnalyzerLanguageCompilerData.Input.Builder {}

        static Builder builder() { return new Builder(); }


        /// Configuration

        @Value.Default default String strategoStrategy() { return "editor-analyze"; }

        @Value.Default default boolean multiFile() { return false; }


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() { return ClassKind.Generated; }


        /// Classes

        @Value.Derived default ResourcePath classesGenDirectory() {
            return languageProject().project().genSourceSpoofaxJavaDirectory();
        }

        // Constraint analyzer

        @Value.Default default TypeInfo genConstraintAnalyzer() {
            return TypeInfo.of(languageProject().packageId(), shared().defaultClassPrefix() + "ConstraintAnalyzer");
        }

        Optional<TypeInfo> manualConstraintAnalyzer();

        default TypeInfo constraintAnalyzer() {
            if(classKind().isManual() && manualConstraintAnalyzer().isPresent()) {
                return manualConstraintAnalyzer().get();
            }
            return genConstraintAnalyzer();
        }

        // Constraint analyzer factory

        @Value.Default default TypeInfo genFactory() {
            return TypeInfo.of(languageProject().packageId(), shared().defaultClassPrefix() + "ConstraintAnalyzerFactory");
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
                genConstraintAnalyzer().file(classesGenDirectory()),
                genFactory().file(classesGenDirectory())
            );
        }


        /// Automatically provided sub-inputs

        Shared shared();

        LanguageProject languageProject();


        @Value.Check default void check() {
            final ClassKind kind = classKind();
            final boolean manual = kind.isManual();
            if(!manual) return;
            if(!manualConstraintAnalyzer().isPresent()) {
                throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualConstraintAnalyzer' has not been set");
            }
            if(!manualFactory().isPresent()) {
                throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualFactory' has not been set");
            }
        }
    }

    @Value.Immutable public interface Output extends Serializable {
        class Builder extends ConstraintAnalyzerLanguageCompilerData.Output.Builder {}

        static Builder builder() { return new Output.Builder(); }
    }
}
