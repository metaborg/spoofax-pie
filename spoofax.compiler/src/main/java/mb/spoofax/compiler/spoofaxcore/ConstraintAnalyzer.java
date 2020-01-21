package mb.spoofax.compiler.spoofaxcore;

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

    public LanguageProjectOutput compileLanguageProject(Input input) throws IOException {
        final LanguageProjectOutput output = LanguageProjectOutput.builder().fromInput(input).build();
        if(input.classKind().isManualOnly()) return output; // Nothing to generate: return.

        final ResourcePath classesGenDirectory = input.languageClassesGenDirectory();
        constraintAnalyzerTemplate.write(input, input.genConstraintAnalyzer().file(classesGenDirectory));
        factoryTemplate.write(input, input.genFactory().file(classesGenDirectory));

        return output;
    }

    public AdapterProjectOutput compileAdapterProject(Input input) throws IOException {
        final AdapterProjectOutput output = AdapterProjectOutput.builder().fromInput(input).build();
        if(input.classKind().isManualOnly()) return output; // Nothing to generate: return.

        final ResourcePath classesGenDirectory = input.adapterClassesGenDirectory();
        analyzeTaskDefTemplate.write(input, input.genAnalyzeTaskDef().file(classesGenDirectory));

        return output;
    }


    @Value.Immutable
    public interface Input extends Serializable {
        class Builder extends ConstraintAnalyzerData.Input.Builder {}

        static Builder builder() {
            return new Builder();
        }


        Shared shared();

        Parser.Input parse();


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
            return TypeInfo.of(shared().languagePackage(), shared().classPrefix() + "ConstraintAnalyzer");
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
            return TypeInfo.of(shared().languagePackage(), shared().classPrefix() + "ConstraintAnalyzerFactory");
        }

        Optional<TypeInfo> manualFactory();

        default TypeInfo factory() {
            if(classKind().isManual() && manualFactory().isPresent()) {
                return manualFactory().get();
            }
            return genFactory();
        }


        /// Adapter project classes

        @Value.Derived default ResourcePath adapterClassesGenDirectory() {
            return shared().adapterProject().genSourceSpoofaxJavaDirectory();
        }

        // Analyze

        @Value.Default default TypeInfo genAnalyzeTaskDef() {
            return TypeInfo.of(shared().adapterTaskPackage(), shared().classPrefix() + "Analyze");
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
    public interface LanguageProjectOutput extends Serializable {
        class Builder extends ConstraintAnalyzerData.LanguageProjectOutput.Builder {
            public Builder fromInput(Input input) {
                final Shared shared = input.shared();
                addDependencies(
                    GradleConfiguredDependency.api(shared.constraintCommonDep())
                );
                return this;
            }
        }

        static Builder builder() {
            return new Builder();
        }


        List<GradleConfiguredDependency> dependencies();

        List<String> copyResources();
    }

    @Value.Immutable
    public interface AdapterProjectOutput extends Serializable {
        class Builder extends ConstraintAnalyzerData.AdapterProjectOutput.Builder {
            public Builder fromInput(Input input) {
                addAdditionalTaskDefs(input.analyzeTaskDef());
                return this;
            }
        }

        static Builder builder() {
            return new Builder();
        }


        List<GradleConfiguredDependency> dependencies();

        List<TypeInfo> additionalTaskDefs();
    }
}
