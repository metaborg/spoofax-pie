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
public class ConstraintAnalyzer {
    private final TemplateWriter constraintAnalyzerTemplate;
    private final TemplateWriter factoryTemplate;
    private final TemplateWriter analyzeTaskDefTemplate;

    public ConstraintAnalyzer(TemplateCompiler templateCompiler) {
        this.constraintAnalyzerTemplate = templateCompiler.getOrCompileToWriter("constraint_analyzer/ConstraintAnalyzer.java.mustache");
        this.factoryTemplate = templateCompiler.getOrCompileToWriter("constraint_analyzer/ConstraintAnalyzerFactory.java.mustache");
        this.analyzeTaskDefTemplate = templateCompiler.getOrCompileToWriter("constraint_analyzer/AnalyzeTaskDef.java.mustache");
    }

    // Language project

    public ListView<GradleConfiguredDependency> getLanguageProjectDependencies(Input input) {
        return ListView.of(GradleConfiguredDependency.api(input.shared().constraintCommonDep()));
    }

    public ListView<String> getLanguageProjectCopyResources(Input input) {
        return ListView.of();
    }

    public Output compileLanguageProject(Input input) throws IOException {
        final Output.Builder outputBuilder = Output.builder();
        if(input.classKind().isManualOnly()) return outputBuilder.build(); // Nothing to generate: return.
        final ResourcePath classesGenDirectory = input.languageClassesGenDirectory();
        outputBuilder.addProvidedResources(
            constraintAnalyzerTemplate.write(input, input.genConstraintAnalyzer().file(classesGenDirectory)),
            factoryTemplate.write(input, input.genFactory().file(classesGenDirectory))
        );
        return outputBuilder.build();
    }

    // Adapter project

    public Output compileAdapterProject(Input input) throws IOException {
        final Output.Builder outputBuilder = Output.builder();
        if(input.classKind().isManualOnly()) return outputBuilder.build(); // Nothing to generate: return.
        final ResourcePath classesGenDirectory = input.adapterClassesGenDirectory();
        outputBuilder.addProvidedResources(
            analyzeTaskDefTemplate.write(input, input.genAnalyzeTaskDef().file(classesGenDirectory))
        );
        return outputBuilder.build();
    }

    // Input

    @Value.Immutable
    public interface Input extends Serializable {
        class Builder extends ConstraintAnalyzerData.Input.Builder {}

        static Builder builder() {
            return new Builder();
        }


        Shared shared();

        Parser.Input parser();


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


        /// Language project classes

        @Value.Derived default ResourcePath languageClassesGenDirectory() {
            return shared().languageProject().genSourceSpoofaxJavaDirectory();
        }

        // Constraint analyzer

        @Value.Default default TypeInfo genConstraintAnalyzer() {
            return TypeInfo.of(shared().languageProjectPackage(), shared().defaultClassPrefix() + "ConstraintAnalyzer");
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
            return TypeInfo.of(shared().languageProjectPackage(), shared().defaultClassPrefix() + "ConstraintAnalyzerFactory");
        }

        Optional<TypeInfo> manualFactory();

        default TypeInfo factory() {
            if(classKind().isManual() && manualFactory().isPresent()) {
                return manualFactory().get();
            }
            return genFactory();
        }

        // List of all generated files for language projects

        default ListView<ResourcePath> generatedLanguageProjectFiles() {
            if(classKind().isManualOnly()) {
                return ListView.of();
            }
            return ListView.of(
                genConstraintAnalyzer().file(languageClassesGenDirectory()),
                genFactory().file(languageClassesGenDirectory())
            );
        }


        /// Adapter project classes

        @Value.Derived default ResourcePath adapterClassesGenDirectory() {
            return shared().adapterProject().genSourceSpoofaxJavaDirectory();
        }

        // Analyze

        @Value.Default default TypeInfo genAnalyzeTaskDef() {
            return TypeInfo.of(shared().adapterProjectTaskPackage(), shared().defaultClassPrefix() + "Analyze");
        }

        Optional<TypeInfo> manualAnalyzeTaskDef();

        default TypeInfo analyzeTaskDef() {
            if(classKind().isManual() && manualAnalyzeTaskDef().isPresent()) {
                return manualAnalyzeTaskDef().get();
            }
            return genAnalyzeTaskDef();
        }


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
            if(!manualAnalyzeTaskDef().isPresent()) {
                throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualAnalyzeTaskDef' has not been set");
            }
        }
    }

    @Value.Immutable
    public interface Output {
        class Builder extends ConstraintAnalyzerData.Output.Builder {}

        static Builder builder() {
            return new Output.Builder();
        }

        List<HierarchicalResource> providedResources();
    }
}
