package mb.spoofax.compiler.spoofaxcore;

import com.samskivert.mustache.Template;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.BuilderBase;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.ResourceWriter;
import mb.spoofax.compiler.util.TemplateCompiler;
import org.immutables.value.Value;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.Properties;

@Value.Enclosing @ImmutablesStyle
public class StylerCompiler {
    private final ResourceService resourceService;
    private final Template stylingRulesTemplate;
    private final Template stylerTemplate;
    private final Template stylerFactoryTemplate;

    private StylerCompiler(ResourceService resourceService, Template stylingRulesTemplate, Template stylerTemplate, Template stylerFactoryTemplate) {
        this.resourceService = resourceService;
        this.stylingRulesTemplate = stylingRulesTemplate;
        this.stylerTemplate = stylerTemplate;
        this.stylerFactoryTemplate = stylerFactoryTemplate;
    }

    public static StylerCompiler fromClassLoaderResources(ResourceService resourceService) {
        final TemplateCompiler templateCompiler = new TemplateCompiler(StylerCompiler.class);
        return new StylerCompiler(
            resourceService,
            templateCompiler.compile("styler/StylingRules.java.mustache"),
            templateCompiler.compile("styler/Styler.java.mustache"),
            templateCompiler.compile("styler/StylerFactory.java.mustache")
        );
    }


    public Output compile(Input input, Charset charset) throws IOException {
        final Output output = Output.builder().withDefaultsBasedOnInput(input).build();
        if(input.classKind().isManualOnly()) return output; // Nothing to generate: return.

        final HierarchicalResource packageDirectory = resourceService.getHierarchicalResource(output.packageDirectory());
        packageDirectory.ensureDirectoryExists();

        final HierarchicalResource stylingRulesFile = resourceService.getHierarchicalResource(output.genStylingRulesFile());
        try(final ResourceWriter writer = new ResourceWriter(stylingRulesFile, charset)) {
            stylingRulesTemplate.execute(input, writer);
            writer.flush();
        }

        final HierarchicalResource stylerFile = resourceService.getHierarchicalResource(output.genStylerFile());
        try(final ResourceWriter writer = new ResourceWriter(stylerFile, charset)) {
            stylerTemplate.execute(input, writer);
            writer.flush();
        }

        final HierarchicalResource stylerFactoryFile = resourceService.getHierarchicalResource(output.genStylerFactoryFile());
        try(final ResourceWriter writer = new ResourceWriter(stylerFactoryFile, charset)) {
            stylerFactoryTemplate.execute(input, writer);
            writer.flush();
        }

        return output;
    }


    @Value.Immutable
    public interface Input extends Serializable {
        class Builder extends StylerCompilerData.Input.Builder implements BuilderBase {
            public Builder withPersistentProperties(Properties properties) {
                with(properties, "genStylingRulesClass", this::genStylingRulesClass);
                with(properties, "genStylerClass", this::genStylerClass);
                with(properties, "genStylerFactoryClass", this::genStylerFactoryClass);
                return this;
            }
        }

        static Builder builder() {
            return new Builder();
        }


        Shared shared();

        JavaProject languageProject();


        @Value.Default default ClassKind classKind() {
            return ClassKind.Generated;
        }

        Optional<String> manualStylerClass();

        Optional<String> manualStylerFactoryClass();


        @Value.Default default String genStylingRulesClass() {
            return shared().classSuffix() + "StylingRules";
        }

        @Value.Derived default String genStylingRulesPath() {
            return genStylingRulesClass() + ".java";
        }

        @Value.Default default String packedESVResourcePath() {
            return languageProject().packagePath() + "/target/metaborg/editor.esv.af";
        }

        @Value.Default default String genStylerClass() {
            return shared().classSuffix() + "Styler";
        }

        @Value.Derived default String genStylerPath() {
            return genStylerClass() + ".java";
        }

        @Value.Default default String genStylerFactoryClass() {
            return shared().classSuffix() + "StylerFactory";
        }

        @Value.Derived default String genStylerFactoryPath() {
            return genStylerFactoryClass() + ".java";
        }


        default void savePersistentProperties(Properties properties) {
            shared().savePersistentProperties(properties);
            properties.setProperty("genStylingRulesClass", genStylingRulesClass());
            properties.setProperty("genStylerClass", genStylerClass());
            properties.setProperty("genStylerFactoryClass", genStylerFactoryClass());
        }

        @Value.Check default void check() {
            final ClassKind kind = classKind();
            final boolean manual = kind.isManual();
            if(!manual) return;
            if(!manualStylerClass().isPresent()) {
                throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualStylerClass' has not been set");
            }
            if(!manualStylerFactoryClass().isPresent()) {
                throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualStylerFactoryClass' has not been set");
            }
        }
    }

    @Value.Immutable
    public interface Output extends Serializable {
        class Builder extends StylerCompilerData.Output.Builder {
            public Builder withDefaultsBasedOnInput(Input input) {
                final ResourcePath javaSourceDirectory = input.languageProject().directory().appendRelativePath("src/main/java");
                final ResourcePath packageDirectory = javaSourceDirectory.appendRelativePath(input.languageProject().packagePath());
                return this
                    .javaSourceDirectory(javaSourceDirectory)
                    .packageDirectory(packageDirectory)
                    .genStylingRulesFile(packageDirectory.appendRelativePath(input.genStylingRulesPath()))
                    .genStylerFile(packageDirectory.appendRelativePath(input.genStylerPath()))
                    .genStylerFactoryFile(packageDirectory.appendRelativePath(input.genStylerFactoryPath()))
                    ;
            }
        }

        static Builder builder() {
            return new Builder();
        }


        ResourcePath javaSourceDirectory();

        ResourcePath packageDirectory();

        ResourcePath genStylingRulesFile();

        ResourcePath genStylerFile();

        ResourcePath genStylerFactoryFile();
    }
}
