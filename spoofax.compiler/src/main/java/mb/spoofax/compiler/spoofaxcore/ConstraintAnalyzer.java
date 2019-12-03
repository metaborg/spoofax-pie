package mb.spoofax.compiler.spoofaxcore;

import com.samskivert.mustache.Template;
import mb.resource.ResourceService;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.ClassInfo;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.GradleConfiguredDependency;
import mb.spoofax.compiler.util.ResourceWriter;
import mb.spoofax.compiler.util.TemplateCompiler;
import org.immutables.value.Value;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;

@Value.Enclosing
public class ConstraintAnalyzer {
    private final Template constraintAnalyzerTemplate;
    private final Template factoryTemplate;
    private final Template analyzeTaskDefTemplate;
    private final ResourceService resourceService;
    private final Charset charset;


    private ConstraintAnalyzer(
        Template constraintAnalyzerTemplate,
        Template factoryTemplate,
        Template analyzeTaskDefTemplate,
        ResourceService resourceService,
        Charset charset
    ) {
        this.analyzeTaskDefTemplate = analyzeTaskDefTemplate;
        this.resourceService = resourceService;
        this.constraintAnalyzerTemplate = constraintAnalyzerTemplate;
        this.factoryTemplate = factoryTemplate;
        this.charset = charset;
    }

    public static ConstraintAnalyzer fromClassLoaderResources(
        ResourceService resourceService,
        Charset charset
    ) {
        final TemplateCompiler templateCompiler = new TemplateCompiler(ConstraintAnalyzer.class);
        return new ConstraintAnalyzer(
            templateCompiler.compile("constraint_analyzer/ConstraintAnalyzer.java.mustache"),
            templateCompiler.compile("constraint_analyzer/ConstraintAnalyzerFactory.java.mustache"),
            templateCompiler.compile("constraint_analyzer/AnalyzeTaskDef.java.mustache"),
            resourceService,
            charset
        );
    }


    public LanguageProjectOutput compileLanguageProject(Input input) throws IOException {
        final LanguageProjectOutput output = LanguageProjectOutput.builder().fromInput(input).build();
        if(input.classKind().isManualOnly()) return output; // Nothing to generate: return.

        final ResourcePath genDirectory = input.languageGenDirectory();
        resourceService.getHierarchicalResource(genDirectory).ensureDirectoryExists();

        try(final ResourceWriter writer = new ResourceWriter(resourceService.getHierarchicalResource(input.genConstraintAnalyzer().file(genDirectory)).createParents(), charset)) {
            constraintAnalyzerTemplate.execute(input, writer);
            writer.flush();
        }

        try(final ResourceWriter writer = new ResourceWriter(resourceService.getHierarchicalResource(input.genFactory().file(genDirectory)).createParents(), charset)) {
            factoryTemplate.execute(input, writer);
            writer.flush();
        }

        return output;
    }

    public AdapterProjectOutput compileAdapterProject(Input input) throws IOException {
        final AdapterProjectOutput output = AdapterProjectOutput.builder().fromInput(input).build();
        if(input.classKind().isManualOnly()) return output; // Nothing to generate: return.

        final ResourcePath genDirectory = input.adapterGenDirectory();
        resourceService.getHierarchicalResource(genDirectory).ensureDirectoryExists();

        try(final ResourceWriter writer = new ResourceWriter(resourceService.getHierarchicalResource(input.genAnalyzeTaskDef().file(genDirectory)).createParents(), charset)) {
            analyzeTaskDefTemplate.execute(input, writer);
            writer.flush();
        }

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

        @Value.Derived default ResourcePath languageGenDirectory() {
            return shared().languageProject().genSourceSpoofaxJavaDirectory();
        }

        default String languageGenPackage() {
            return shared().languageProject().packageId();
        }

        // Constraint analyzer

        @Value.Default default ClassInfo genConstraintAnalyzer() {
            return ClassInfo.of(languageGenPackage(), shared().classSuffix() + "ConstraintAnalyzer");
        }

        Optional<ClassInfo> manualConstraintAnalyzer();

        default ClassInfo constraintAnalyzer() {
            if(classKind().isManual() && manualConstraintAnalyzer().isPresent()) {
                return manualConstraintAnalyzer().get();
            }
            return genConstraintAnalyzer();
        }

        // Constraint analyzer factory

        @Value.Default default ClassInfo genFactory() {
            return ClassInfo.of(languageGenPackage(), shared().classSuffix() + "ConstraintAnalyzerFactory");
        }

        Optional<ClassInfo> manualFactory();

        default ClassInfo factory() {
            if(classKind().isManual() && manualFactory().isPresent()) {
                return manualFactory().get();
            }
            return genFactory();
        }


        /// Adapter project classes

        @Value.Derived default ResourcePath adapterGenDirectory() {
            return shared().adapterProject().genSourceSpoofaxJavaDirectory();
        }

        default String taskDefGenPackage() {
            return shared().adapterProject().packageId() + ".taskdef";
        }

        // Analyze

        @Value.Default default ClassInfo genAnalyzeTaskDef() {
            return ClassInfo.of(taskDefGenPackage(), shared().classSuffix() + "Analyze");
        }

        Optional<ClassInfo> manualAnalyzeTaskDef();

        default ClassInfo analyzeTaskDef() {
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
                addDependencies(GradleConfiguredDependency.api(input.shared().constraintCommonDep()));
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

        List<ClassInfo> additionalTaskDefs();
    }
}
