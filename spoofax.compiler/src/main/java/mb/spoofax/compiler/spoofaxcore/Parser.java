package mb.spoofax.compiler.spoofaxcore;

import com.samskivert.mustache.Template;
import mb.resource.hierarchical.HierarchicalResource;
import mb.spoofax.compiler.util.*;
import org.immutables.value.Value;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.Properties;

public class Parser {
    @Value.Style(visibility = Value.Style.ImplementationVisibility.PACKAGE, overshadowImplementation = true)
    @Value.Immutable
    public interface Input {
        Coordinates coordinates();


        @Value.Default default ClassKind kind() {
            return ClassKind.Generated;
        }

        Optional<String> manualParserClass();

        Optional<String> manualParserFactoryClass();


        @Value.Default default String genTableClass() {
            return coordinates().classSuffix() + "ParseTable";
        }

        @Value.Default default String genTableResourcePath() {
            return coordinates().packagePath() + "/target/metaborg/sdf.tbl";
        }

        @Value.Default default String genParserClass() {
            return coordinates().classSuffix() + "Parser";
        }

        @Value.Default default String genParserFactoryClass() {
            return coordinates().classSuffix() + "ParserFactory";
        }


        default void savePersistentProperties(Properties properties) {
            properties.setProperty("genTableClass", genTableClass());
            properties.setProperty("genParserClass", genParserClass());
            properties.setProperty("genParserFactoryClass", genParserFactoryClass());
        }

        class Builder extends ImmutableInput.Builder implements BuilderBase {
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

        @Value.Check
        default void check() {
            final ClassKind kind = kind();
            final boolean manual = kind == ClassKind.Manual || kind == ClassKind.Extended;
            if(!manual) return;
            if(!manualParserClass().isPresent()) {
                throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualParserClass' has not been set");
            }
            if(!manualParserFactoryClass().isPresent()) {
                throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualParserFactoryClass' has not been set");
            }
        }
    }

    public static class Compiler {
        private final Template parseTableTemplate;
        private final Template parserTemplate;
        private final Template parserFactoryTemplate;

        private Compiler(Template parseTableTemplate, Template parserTemplate, Template parserFactoryTemplate) {
            this.parseTableTemplate = parseTableTemplate;
            this.parserTemplate = parserTemplate;
            this.parserFactoryTemplate = parserFactoryTemplate;
        }

        public static Compiler fromClassLoaderResources() {
            final TemplateCompiler templateCompiler = new TemplateCompiler(Parser.class);
            return new Compiler(
                templateCompiler.compile("ParseTable.java.mustache"),
                templateCompiler.compile("Parser.java.mustache"),
                templateCompiler.compile("ParserFactory.java.mustache")
            );
        }


        public ResourceDeps compile(Input input, HierarchicalResource baseDir, Charset charset) throws IOException {
            final HierarchicalResource pkgDir = getPackageDir(input, baseDir);
            pkgDir.createDirectory(true);
            final HierarchicalResource parseTable = getParseTableFile(pkgDir);
            try(final ResourceWriter writer = new ResourceWriter(parseTable, charset)) {
                parseTableTemplate.execute(input, input.coordinates(), writer);
                writer.flush();
            }
            final HierarchicalResource parser = getParserFile(pkgDir);
            try(final ResourceWriter writer = new ResourceWriter(parser, charset)) {
                parserTemplate.execute(input, input.coordinates(), writer);
                writer.flush();
            }
            final HierarchicalResource parserFactory = getParserFactoryFile(pkgDir);
            try(final ResourceWriter writer = new ResourceWriter(parserFactory, charset)) {
                parserFactoryTemplate.execute(input, input.coordinates(), writer);
                writer.flush();
            }
            return ImmutableResourceDeps.builder().addProvidedResources(parseTable, parser, parserFactory).build();
        }


        public HierarchicalResource getPackageDir(Input input, HierarchicalResource baseDir) {
            return baseDir.appendRelativePath(input.coordinates().packagePath());
        }

        public HierarchicalResource getParseTableFile(HierarchicalResource packageDir) {
            return packageDir.appendSegment("ParseTable.java");
        }

        public HierarchicalResource getParserFile(HierarchicalResource packageDir) {
            return packageDir.appendSegment("Parser.java");
        }

        public HierarchicalResource getParserFactoryFile(HierarchicalResource packageDir) {
            return packageDir.appendSegment("ParserFactory.java");
        }
    }

}