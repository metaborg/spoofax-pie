package mb.spoofax.compiler.spoofaxcore;

import com.samskivert.mustache.Template;
import mb.resource.ResourceService;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.BuilderBase;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.GradleConfiguredDependency;
import mb.spoofax.compiler.util.GradleProject;
import mb.spoofax.compiler.util.ResourceWriter;
import mb.spoofax.compiler.util.TemplateCompiler;
import org.immutables.value.Value;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

@Value.Enclosing
public class ConstraintAnalyzer {
    private final Template constraintAnalyzerTemplate;
    private final Template factoryTemplate;
    private final ResourceService resourceService;
    private final Charset charset;


    private ConstraintAnalyzer(Template constraintAnalyzerTemplate, Template factoryTemplate, ResourceService resourceService, Charset charset) {
        this.resourceService = resourceService;
        this.constraintAnalyzerTemplate = constraintAnalyzerTemplate;
        this.factoryTemplate = factoryTemplate;
        this.charset = charset;
    }

    public static ConstraintAnalyzer fromClassLoaderResources(ResourceService resourceService, Charset charset) {
        final TemplateCompiler templateCompiler = new TemplateCompiler(ConstraintAnalyzer.class);
        return new ConstraintAnalyzer(
            templateCompiler.compile("constraint_analyzer/ConstraintAnalyzer.java.mustache"),
            templateCompiler.compile("constraint_analyzer/ConstraintAnalyzerFactory.java.mustache"),
            resourceService,
            charset
        );
    }


    public LanguageProjectOutput compileLanguageProject(Input input) throws IOException {
        final LanguageProjectOutput output = LanguageProjectOutput.builder().fromInput(input).build();
        if(input.classKind().isManualOnly()) return output; // Nothing to generate: return.

        resourceService.getHierarchicalResource(input.genDirectory()).ensureDirectoryExists();

        try(final ResourceWriter writer = new ResourceWriter(resourceService.getHierarchicalResource(input.genConstraintAnalyzerFile()), charset)) {
            constraintAnalyzerTemplate.execute(input, writer);
            writer.flush();
        }

        try(final ResourceWriter writer = new ResourceWriter(resourceService.getHierarchicalResource(input.genFactoryFile()), charset)) {
            factoryTemplate.execute(input, writer);
            writer.flush();
        }

        return output;
    }

    public AdapterProjectOutput compileAdapterProject(Input input) throws IOException {
        return AdapterProjectOutput.builder().build();
    }


    @Value.Immutable
    public interface Input extends Serializable {
        class Builder extends ConstraintAnalyzerData.Input.Builder implements BuilderBase {
            public Builder withPersistentProperties(Properties properties) {
                with(properties, "genConstraintAnalyzerClass", this::genConstraintAnalyzerClass);
                with(properties, "genFactoryClass", this::genFactoryClass);
                return this;
            }
        }

        static Builder builder() {
            return new Builder();
        }


        Shared shared();


        @Value.Default default String strategoStrategy() {
            return "editor-analyze";
        }

        @Value.Default default boolean multiFile() {
            return false;
        }


        @Value.Default default ClassKind classKind() {
            return ClassKind.Generated;
        }

        @Value.Derived default ResourcePath genDirectory() {
            final GradleProject languageProject = shared().languageProject();
            return languageProject.genSourceSpoofaxJavaDirectory().appendRelativePath(languageProject.packagePath());
        }


        @Value.Default default String genConstraintAnalyzerClass() {
            return shared().classSuffix() + "ConstraintAnalyzer";
        }

        @Value.Derived default String genConstraintAnalyzerFileName() {
            return genConstraintAnalyzerClass() + ".java";
        }

        @Value.Derived default ResourcePath genConstraintAnalyzerFile() {
            return genDirectory().appendSegment(genConstraintAnalyzerFileName());
        }

        Optional<String> manualConstraintAnalyzerClass();

        @Value.Derived default String constraintAnalyzerClass() {
            if(classKind().isManual() && manualConstraintAnalyzerClass().isPresent()) {
                return manualConstraintAnalyzerClass().get();
            }
            return genConstraintAnalyzerFileName();
        }


        @Value.Default default String genFactoryClass() {
            return shared().classSuffix() + "ConstraintAnalyzerFactory";
        }

        @Value.Derived default String genFactoryFileName() {
            return genFactoryClass() + ".java";
        }

        @Value.Derived default ResourcePath genFactoryFile() {
            return genDirectory().appendSegment(genFactoryFileName());
        }

        Optional<String> manualFactoryClass();

        @Value.Derived default String factoryClass() {
            if(classKind().isManual() && manualFactoryClass().isPresent()) {
                return manualFactoryClass().get();
            }
            return genFactoryClass();
        }


        default void savePersistentProperties(Properties properties) {
            shared().savePersistentProperties(properties);
            properties.setProperty("genConstraintAnalyzerClass", genConstraintAnalyzerClass());
            properties.setProperty("genFactoryClass", genFactoryClass());
        }

        @Value.Check default void check() {
            final ClassKind kind = classKind();
            final boolean manual = kind.isManual();
            if(!manual) return;
            if(!manualConstraintAnalyzerClass().isPresent()) {
                throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualConstraintAnalyzerClass' has not been set");
            }
            if(!manualFactoryClass().isPresent()) {
                throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualFactoryClass' has not been set");
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

        }

        static Builder builder() {
            return new Builder();
        }
    }
}
