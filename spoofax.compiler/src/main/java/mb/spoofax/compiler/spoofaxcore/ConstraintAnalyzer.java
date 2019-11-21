package mb.spoofax.compiler.spoofaxcore;

import com.samskivert.mustache.Template;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.BuilderBase;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.JavaProject;
import mb.spoofax.compiler.util.ResourceWriter;
import mb.spoofax.compiler.util.TemplateCompiler;
import org.immutables.value.Value;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Optional;
import java.util.Properties;

import static mb.spoofax.compiler.util.StringUtil.doubleQuote;

@Value.Enclosing
public class ConstraintAnalyzer {
    private final ResourceService resourceService;
    private final Template constraintAnalyzerTemplate;
    private final Template factoryTemplate;

    private ConstraintAnalyzer(ResourceService resourceService, Template constraintAnalyzerTemplate, Template factoryTemplate) {
        this.resourceService = resourceService;
        this.constraintAnalyzerTemplate = constraintAnalyzerTemplate;
        this.factoryTemplate = factoryTemplate;
    }

    public static ConstraintAnalyzer fromClassLoaderResources(ResourceService resourceService) {
        final TemplateCompiler templateCompiler = new TemplateCompiler(ConstraintAnalyzer.class);
        return new ConstraintAnalyzer(
            resourceService,
            templateCompiler.compile("constraint_analyzer/ConstraintAnalyzer.java.mustache"),
            templateCompiler.compile("constraint_analyzer/ConstraintAnalyzerFactory.java.mustache")
        );
    }


    public Output compile(Input input, Charset charset) throws IOException {
        final Output output = Output.builder().withDefaultsBasedOnInput(input).build();
        if(input.classKind().isManualOnly()) return output; // Nothing to generate: return.

        final HashMap<String, Object> map = new HashMap<>();
        map.put("strategoStrategyCode", doubleQuote(input.strategoStrategy()));

        final HierarchicalResource genSourcesJavaDirectory = resourceService.getHierarchicalResource(output.genSourcesJavaDirectory());
        genSourcesJavaDirectory.ensureDirectoryExists();

        final HierarchicalResource constraintAnalyzerFile = resourceService.getHierarchicalResource(output.genConstraintAnalyzerFile());
        try(final ResourceWriter writer = new ResourceWriter(constraintAnalyzerFile, charset)) {
            constraintAnalyzerTemplate.execute(input, map, writer);
            writer.flush();
        }

        final HierarchicalResource factoryFile = resourceService.getHierarchicalResource(output.genFactoryFile());
        try(final ResourceWriter writer = new ResourceWriter(factoryFile, charset)) {
            factoryTemplate.execute(input, writer);
            writer.flush();
        }

        return output;
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

        JavaProject languageProject();


        @Value.Default default String strategoStrategy() {
            return "editor-analyze";
        }

        @Value.Default default boolean multiFile() {
            return false;
        }


        @Value.Default default ClassKind classKind() {
            return ClassKind.Generated;
        }

        Optional<String> manualConstraintAnalyzerClass();

        Optional<String> manualFactoryClass();


        @Value.Default default String genConstraintAnalyzerClass() {
            return shared().classSuffix() + "ConstraintAnalyzer";
        }

        @Value.Derived default String genConstraintAnalyzerPath() {
            return genConstraintAnalyzerClass() + ".java";
        }

        @Value.Default default String genFactoryClass() {
            return shared().classSuffix() + "ConstraintAnalyzerFactory";
        }

        @Value.Derived default String genFactoryPath() {
            return genFactoryClass() + ".java";
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
    public interface Output extends Serializable {
        class Builder extends ConstraintAnalyzerData.Output.Builder {
            public Builder withDefaultsBasedOnInput(Input input) {
                final ResourcePath genSourcesJavaDirectory = input.languageProject().genSourceSpoofaxJavaDirectory().appendRelativePath(input.languageProject().packagePath());
                return this
                    .genSourcesJavaDirectory(genSourcesJavaDirectory)
                    .genConstraintAnalyzerFile(genSourcesJavaDirectory.appendRelativePath(input.genConstraintAnalyzerPath()))
                    .genFactoryFile(genSourcesJavaDirectory.appendRelativePath(input.genFactoryPath()))
                    ;
            }
        }

        static Builder builder() {
            return new Builder();
        }


        ResourcePath genSourcesJavaDirectory();

        ResourcePath genConstraintAnalyzerFile();

        ResourcePath genFactoryFile();
    }
}
