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
import java.util.Optional;
import java.util.Properties;

@Value.Enclosing
public class Styler {
    private final ResourceService resourceService;
    private final Template rulesTemplate;
    private final Template stylerTemplate;
    private final Template factoryTemplate;

    private Styler(ResourceService resourceService, Template rulesTemplate, Template stylerTemplate, Template factoryTemplate) {
        this.resourceService = resourceService;
        this.rulesTemplate = rulesTemplate;
        this.stylerTemplate = stylerTemplate;
        this.factoryTemplate = factoryTemplate;
    }

    public static Styler fromClassLoaderResources(ResourceService resourceService) {
        final TemplateCompiler templateCompiler = new TemplateCompiler(Styler.class);
        return new Styler(
            resourceService,
            templateCompiler.compile("styler/StylingRules.java.mustache"),
            templateCompiler.compile("styler/Styler.java.mustache"),
            templateCompiler.compile("styler/StylerFactory.java.mustache")
        );
    }


    public Output compile(Input input, Charset charset) throws IOException {
        final Output output = Output.builder().withDefaultsBasedOnInput(input).build();
        if(input.classKind().isManualOnly()) return output; // Nothing to generate: return.

        final HierarchicalResource genSourcesJavaDirectory = resourceService.getHierarchicalResource(output.genSourcesJavaDirectory());
        genSourcesJavaDirectory.ensureDirectoryExists();

        final HierarchicalResource rulesFile = resourceService.getHierarchicalResource(output.genRulesFile());
        try(final ResourceWriter writer = new ResourceWriter(rulesFile, charset)) {
            rulesTemplate.execute(input, writer);
            writer.flush();
        }

        final HierarchicalResource stylerFile = resourceService.getHierarchicalResource(output.genStylerFile());
        try(final ResourceWriter writer = new ResourceWriter(stylerFile, charset)) {
            stylerTemplate.execute(input, writer);
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
        class Builder extends StylerData.Input.Builder implements BuilderBase {
            public Builder withPersistentProperties(Properties properties) {
                with(properties, "genRulesClass", this::genRulesClass);
                with(properties, "genStylerClass", this::genStylerClass);
                with(properties, "genFactoryClass", this::genFactoryClass);
                return this;
            }
        }

        static Builder builder() {
            return new Builder();
        }


        Shared shared();

        JavaProject languageProject();


        @Value.Default default String packedESVResourcePath() {
            return languageProject().packagePath() + "/target/metaborg/editor.esv.af";
        }


        @Value.Default default ClassKind classKind() {
            return ClassKind.Generated;
        }

        Optional<String> manualStylerClass();

        Optional<String> manualFactoryClass();

        @Value.Default default String genRulesClass() {
            return shared().classSuffix() + "StylingRules";
        }

        @Value.Derived default String genRulesPath() {
            return genRulesClass() + ".java";
        }

        @Value.Default default String genStylerClass() {
            return shared().classSuffix() + "Styler";
        }

        @Value.Derived default String genStylerPath() {
            return genStylerClass() + ".java";
        }

        @Value.Default default String genFactoryClass() {
            return shared().classSuffix() + "StylerFactory";
        }

        @Value.Derived default String genFactoryPath() {
            return genFactoryClass() + ".java";
        }


        default void savePersistentProperties(Properties properties) {
            shared().savePersistentProperties(properties);
            properties.setProperty("genRulesClass", genRulesClass());
            properties.setProperty("genStylerClass", genStylerClass());
            properties.setProperty("genFactoryClass", genFactoryClass());
        }

        @Value.Check default void check() {
            final ClassKind kind = classKind();
            final boolean manual = kind.isManual();
            if(!manual) return;
            if(!manualStylerClass().isPresent()) {
                throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualStylerClass' has not been set");
            }
            if(!manualFactoryClass().isPresent()) {
                throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualFactoryClass' has not been set");
            }
        }
    }

    @Value.Immutable
    public interface Output extends Serializable {
        class Builder extends StylerData.Output.Builder {
            public Builder withDefaultsBasedOnInput(Input input) {
                final ResourcePath genSourcesJavaDirectory = input.languageProject().genSourceSpoofaxJavaDirectory().appendRelativePath(input.languageProject().packagePath());
                return this
                    .genSourcesJavaDirectory(genSourcesJavaDirectory)
                    .genRulesFile(genSourcesJavaDirectory.appendRelativePath(input.genRulesPath()))
                    .genStylerFile(genSourcesJavaDirectory.appendRelativePath(input.genStylerPath()))
                    .genFactoryFile(genSourcesJavaDirectory.appendRelativePath(input.genFactoryPath()))
                    ;
            }
        }

        static Builder builder() {
            return new Builder();
        }


        ResourcePath genSourcesJavaDirectory();

        ResourcePath genRulesFile();

        ResourcePath genStylerFile();

        ResourcePath genFactoryFile();
    }
}
