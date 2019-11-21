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
public class Parser {
    private final ResourceService resourceService;
    private final Template tableTemplate;
    private final Template parserTemplate;
    private final Template factoryTemplate;

    private Parser(ResourceService resourceService, Template tableTemplate, Template parserTemplate, Template factoryTemplate) {
        this.resourceService = resourceService;
        this.tableTemplate = tableTemplate;
        this.parserTemplate = parserTemplate;
        this.factoryTemplate = factoryTemplate;
    }

    public static Parser fromClassLoaderResources(ResourceService resourceService) {
        final TemplateCompiler templateCompiler = new TemplateCompiler(Parser.class);
        return new Parser(
            resourceService,
            templateCompiler.compile("parser/ParseTable.java.mustache"),
            templateCompiler.compile("parser/Parser.java.mustache"),
            templateCompiler.compile("parser/ParserFactory.java.mustache")
        );
    }


    public Output compile(Input input, Charset charset) throws IOException {
        final Output output = Output.builder().withDefaultsBasedOnInput(input).build();
        if(input.classKind().isManualOnly()) return output; // Nothing to generate: return.

        final HierarchicalResource genSourcesJavaDirectory = resourceService.getHierarchicalResource(output.genSourcesJavaDirectory());
        genSourcesJavaDirectory.ensureDirectoryExists();

        final HierarchicalResource tableFile = resourceService.getHierarchicalResource(output.genTableFile());
        try(final ResourceWriter writer = new ResourceWriter(tableFile, charset)) {
            tableTemplate.execute(input, writer);
            writer.flush();
        }

        final HierarchicalResource parserFile = resourceService.getHierarchicalResource(output.genParserFile());
        try(final ResourceWriter writer = new ResourceWriter(parserFile, charset)) {
            parserTemplate.execute(input, writer);
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
        class Builder extends ParserData.Input.Builder implements BuilderBase {
            public Builder withPersistentProperties(Properties properties) {
                with(properties, "genTableClass", this::genTableClass);
                with(properties, "genParserClass", this::genParserClass);
                with(properties, "genFactoryClass", this::genFactoryClass);
                return this;
            }
        }

        static Builder builder() {
            return new Builder();
        }


        Shared shared();

        JavaProject languageProject();


        @Value.Default default String tableResourcePath() {
            return languageProject().packagePath() + "/target/metaborg/sdf.tbl";
        }


        @Value.Default default ClassKind classKind() {
            return ClassKind.Generated;
        }

        Optional<String> manualParserClass();

        Optional<String> manualFactoryClass();

        @Value.Default default String genTableClass() {
            return shared().classSuffix() + "ParseTable";
        }

        @Value.Derived default String genTablePath() {
            return genTableClass() + ".java";
        }

        @Value.Default default String genParserClass() {
            return shared().classSuffix() + "Parser";
        }

        @Value.Derived default String genParserPath() {
            return genParserClass() + ".java";
        }

        @Value.Default default String genFactoryClass() {
            return shared().classSuffix() + "ParserFactory";
        }

        @Value.Derived default String genFactoryPath() {
            return genFactoryClass() + ".java";
        }


        default void savePersistentProperties(Properties properties) {
            shared().savePersistentProperties(properties);
            properties.setProperty("genTableClass", genTableClass());
            properties.setProperty("genParserClass", genParserClass());
            properties.setProperty("genFactoryClass", genFactoryClass());
        }

        @Value.Check default void check() {
            final ClassKind kind = classKind();
            final boolean manual = kind.isManual();
            if(!manual) return;
            if(!manualParserClass().isPresent()) {
                throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualParserClass' has not been set");
            }
            if(!manualFactoryClass().isPresent()) {
                throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualFactoryClass' has not been set");
            }
        }
    }

    @Value.Immutable
    public interface Output extends Serializable {
        class Builder extends ParserData.Output.Builder {
            public Builder withDefaultsBasedOnInput(Input input) {
                final ResourcePath genSourcesJavaDirectory = input.languageProject().genSourceSpoofaxJavaDirectory().appendRelativePath(input.languageProject().packagePath());
                return this
                    .genSourcesJavaDirectory(genSourcesJavaDirectory)
                    .genTableFile(genSourcesJavaDirectory.appendRelativePath(input.genTablePath()))
                    .genParserFile(genSourcesJavaDirectory.appendRelativePath(input.genParserPath()))
                    .genFactoryFile(genSourcesJavaDirectory.appendRelativePath(input.genFactoryPath()))
                    ;
            }
        }

        static Builder builder() {
            return new Builder();
        }


        ResourcePath genSourcesJavaDirectory();

        ResourcePath genTableFile();

        ResourcePath genParserFile();

        ResourcePath genFactoryFile();
    }
}
