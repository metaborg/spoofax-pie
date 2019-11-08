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

@Value.Enclosing
public class ParserCompiler {
    private final ResourceService resourceService;
    private final Template parseTableTemplate;
    private final Template parserTemplate;
    private final Template parserFactoryTemplate;

    private ParserCompiler(ResourceService resourceService, Template parseTableTemplate, Template parserTemplate, Template parserFactoryTemplate) {
        this.resourceService = resourceService;
        this.parseTableTemplate = parseTableTemplate;
        this.parserTemplate = parserTemplate;
        this.parserFactoryTemplate = parserFactoryTemplate;
    }

    public static ParserCompiler fromClassLoaderResources(ResourceService resourceService) {
        final TemplateCompiler templateCompiler = new TemplateCompiler(ParserCompiler.class);
        return new ParserCompiler(
            resourceService,
            templateCompiler.compile("parser/ParseTable.java.mustache"),
            templateCompiler.compile("parser/Parser.java.mustache"),
            templateCompiler.compile("parser/ParserFactory.java.mustache")
        );
    }


    public Output compile(Input input, Charset charset) throws IOException {
        final Output output = Output.builder().withDefaultsBasedOnInput(input).build();
        if(input.classKind().isManualOnly()) return output; // Nothing to generate: return.

        final HierarchicalResource packageDirectory = resourceService.getHierarchicalResource(output.packageDirectory());
        packageDirectory.ensureDirectoryExists();

        final HierarchicalResource parseTableFile = resourceService.getHierarchicalResource(output.genParseTableFile());
        try(final ResourceWriter writer = new ResourceWriter(parseTableFile, charset)) {
            parseTableTemplate.execute(input, writer);
            writer.flush();
        }

        final HierarchicalResource parserFile = resourceService.getHierarchicalResource(output.genParserFile());
        try(final ResourceWriter writer = new ResourceWriter(parserFile, charset)) {
            parserTemplate.execute(input, writer);
            writer.flush();
        }

        final HierarchicalResource parserFactoryFile = resourceService.getHierarchicalResource(output.genParserFactoryFile());
        try(final ResourceWriter writer = new ResourceWriter(parserFactoryFile, charset)) {
            parserFactoryTemplate.execute(input, writer);
            writer.flush();
        }

        return output;
    }


    @Value.Immutable
    public interface Input extends Serializable {
        class Builder extends ParserCompilerData.Input.Builder implements BuilderBase {
            public Builder withPersistentProperties(Properties properties) {
                with(properties, "genTableClass", this::genTableClass);
                with(properties, "genParserClass", this::genParserClass);
                with(properties, "genParserFactoryClass", this::genParserFactoryClass);
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

        Optional<String> manualParserClass();

        Optional<String> manualParserFactoryClass();


        @Value.Default default String genTableClass() {
            return shared().classSuffix() + "ParseTable";
        }

        @Value.Derived default String genTablePath() {
            return genTableClass() + ".java";
        }

        @Value.Default default String tableResourcePath() {
            return languageProject().packagePath() + "/target/metaborg/sdf.tbl";
        }

        @Value.Default default String genParserClass() {
            return shared().classSuffix() + "Parser";
        }

        @Value.Derived default String genParserPath() {
            return genParserClass() + ".java";
        }

        @Value.Default default String genParserFactoryClass() {
            return shared().classSuffix() + "ParserFactory";
        }

        @Value.Derived default String genParserFactoryPath() {
            return genParserFactoryClass() + ".java";
        }


        default void savePersistentProperties(Properties properties) {
            shared().savePersistentProperties(properties);
            properties.setProperty("genTableClass", genTableClass());
            properties.setProperty("genParserClass", genParserClass());
            properties.setProperty("genParserFactoryClass", genParserFactoryClass());
        }

        @Value.Check default void check() {
            final ClassKind kind = classKind();
            final boolean manual = kind.isManual();
            if(!manual) return;
            if(!manualParserClass().isPresent()) {
                throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualParserClass' has not been set");
            }
            if(!manualParserFactoryClass().isPresent()) {
                throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualParserFactoryClass' has not been set");
            }
        }
    }

    @Value.Immutable
    public interface Output extends Serializable {
        class Builder extends ParserCompilerData.Output.Builder {
            public Builder withDefaultsBasedOnInput(Input input) {
                final ResourcePath javaSourceDirectory = input.languageProject().directory().appendRelativePath("src/main/java");
                final ResourcePath packageDirectory = javaSourceDirectory.appendRelativePath(input.languageProject().packagePath());
                return this
                    .javaSourceDirectory(javaSourceDirectory)
                    .packageDirectory(packageDirectory)
                    .genParseTableFile(packageDirectory.appendRelativePath(input.genTablePath()))
                    .genParserFile(packageDirectory.appendRelativePath(input.genParserPath()))
                    .genParserFactoryFile(packageDirectory.appendRelativePath(input.genParserFactoryPath()))
                    ;
            }
        }

        static Builder builder() {
            return new Builder();
        }


        ResourcePath javaSourceDirectory();

        ResourcePath packageDirectory();

        ResourcePath genParseTableFile();

        ResourcePath genParserFile();

        ResourcePath genParserFactoryFile();
    }
}
