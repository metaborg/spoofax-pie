package mb.spoofax.compiler.spoofaxcore;

import mb.common.util.ListView;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.GradleConfiguredDependency;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.compiler.util.TemplateWriter;
import mb.spoofax.compiler.util.TypeInfo;
import org.immutables.value.Value;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@Value.Enclosing
public class ConstraintAnalyzerCompiler {
    private final TemplateWriter constraintAnalyzerTemplate;
    private final TemplateWriter factoryTemplate;
    private final TemplateWriter analyzeTaskDefTemplate;
    private final TemplateWriter analyzeMultiTaskDefTemplate;

    public ConstraintAnalyzerCompiler(TemplateCompiler templateCompiler) {
        this.constraintAnalyzerTemplate = templateCompiler.getOrCompileToWriter("constraint_analyzer/ConstraintAnalyzer.java.mustache");
        this.factoryTemplate = templateCompiler.getOrCompileToWriter("constraint_analyzer/ConstraintAnalyzerFactory.java.mustache");
        this.analyzeTaskDefTemplate = templateCompiler.getOrCompileToWriter("constraint_analyzer/AnalyzeTaskDef.java.mustache");
        this.analyzeMultiTaskDefTemplate = templateCompiler.getOrCompileToWriter("constraint_analyzer/AnalyzeMultiTaskDef.java.mustache");
    }

    // Language project

    public ListView<GradleConfiguredDependency> getLanguageProjectDependencies(LanguageProjectInput input) {
        return ListView.of(GradleConfiguredDependency.api(input.shared().constraintCommonDep()));
    }

    public ListView<String> getLanguageProjectCopyResources(LanguageProjectInput input) {
        return ListView.of();
    }

    public Output compileLanguageProject(LanguageProjectInput input) throws IOException {
        final Output.Builder outputBuilder = Output.builder();
        if(input.classKind().isManualOnly()) return outputBuilder.build(); // Nothing to generate: return.
        final ResourcePath classesGenDirectory = input.classesGenDirectory();
        outputBuilder.addProvidedResources(
            constraintAnalyzerTemplate.write(input.genConstraintAnalyzer().file(classesGenDirectory), input),
            factoryTemplate.write(input.genFactory().file(classesGenDirectory), input)
        );
        return outputBuilder.build();
    }

    // Adapter project

    public ListView<GradleConfiguredDependency> getAdapterProjectDependencies(AdapterProjectInput input) {
        return ListView.of(GradleConfiguredDependency.api(input.shared().constraintPieDep()));
    }

    public Output compileAdapterProject(AdapterProjectInput input) throws IOException {
        final Output.Builder outputBuilder = Output.builder();
        if(input.classKind().isManualOnly()) return outputBuilder.build(); // Nothing to generate: return.
        final ResourcePath classesGenDirectory = input.classesGenDirectory();
        outputBuilder.addProvidedResources(
            analyzeTaskDefTemplate.write(input.genAnalyzeTaskDef().file(classesGenDirectory), input),
            analyzeMultiTaskDefTemplate.write(input.genAnalyzeMultiTaskDef().file(classesGenDirectory), input)
        );
        return outputBuilder.build();
    }

    // Inputs

    @Value.Immutable
    public interface LanguageProjectInput extends Serializable {
        class Builder extends ConstraintAnalyzerCompilerData.LanguageProjectInput.Builder {}

        static Builder builder() {
            return new Builder();
        }


        /// Configuration

        @Value.Default default String strategoStrategy() {
            return "editor-analyze";
        }

        @Value.Default default boolean multiFile() {
            return false;
        }


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() {
            return ClassKind.Generated;
        }


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

    @Value.Immutable
    public interface AdapterProjectInput extends Serializable {
        class Builder extends ConstraintAnalyzerCompilerData.AdapterProjectInput.Builder {}

        static Builder builder() {
            return new Builder();
        }


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() {
            return ClassKind.Generated;
        }


        /// Classes

        @Value.Derived default ResourcePath classesGenDirectory() {
            return adapterProject().project().genSourceSpoofaxJavaDirectory();
        }

        // Analyze

        @Value.Default default TypeInfo genAnalyzeTaskDef() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "Analyze");
        }

        Optional<TypeInfo> manualAnalyzeTaskDef();

        default TypeInfo analyzeTaskDef() {
            if(classKind().isManual() && manualAnalyzeTaskDef().isPresent()) {
                return manualAnalyzeTaskDef().get();
            }
            return genAnalyzeTaskDef();
        }

        // Multi-file analyze

        @Value.Default default TypeInfo genAnalyzeMultiTaskDef() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "AnalyzeMulti");
        }

        Optional<TypeInfo> manualAnalyzeMultiTaskDef();

        default TypeInfo analyzeMultiTaskDef() {
            if(classKind().isManual() && manualAnalyzeMultiTaskDef().isPresent()) {
                return manualAnalyzeMultiTaskDef().get();
            }
            return genAnalyzeMultiTaskDef();
        }


        // List of all generated files

        default ListView<ResourcePath> generatedFiles() {
            if(classKind().isManualOnly()) {
                return ListView.of();
            }
            return ListView.of(
                genAnalyzeTaskDef().file(classesGenDirectory()),
                genAnalyzeMultiTaskDef().file(classesGenDirectory())
            );
        }


        /// Automatically provided sub-inputs

        Shared shared();

        AdapterProject adapterProject();

        LanguageProjectInput languageProjectInput();


        @Value.Check default void check() {
            final ClassKind kind = classKind();
            final boolean manual = kind.isManualOnly();
            if(!manual) return;
            if(!manualAnalyzeTaskDef().isPresent()) {
                throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualAnalyzeTaskDef' has not been set");
            }
        }
    }

    @Value.Immutable
    public interface Output {
        class Builder extends ConstraintAnalyzerCompilerData.Output.Builder {}

        static Builder builder() {
            return new Output.Builder();
        }

        List<HierarchicalResource> providedResources();
    }
}
