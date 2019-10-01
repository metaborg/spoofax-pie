package mb.spoofax.compiler.spoofaxcore;

import com.samskivert.mustache.Template;
import mb.resource.hierarchical.HierarchicalResource;
import mb.spoofax.compiler.util.BuilderBase;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.ResourceWriter;
import mb.spoofax.compiler.util.TemplateCompiler;
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


        public void compile(Input input, HierarchicalResource baseDir, Charset charset) throws IOException {
            final HierarchicalResource pkgDir = baseDir.appendRelativePath(input.coordinates().packagePath());
            pkgDir.createDirectory(true);
            try(final ResourceWriter writer = new ResourceWriter(pkgDir.appendSegment("ParseTable.java"), charset)) {
                parseTableTemplate.execute(input, input.coordinates(), writer);
                writer.flush();
            }
            try(final ResourceWriter writer = new ResourceWriter(pkgDir.appendSegment("Parser.java"), charset)) {
                parserTemplate.execute(input, input.coordinates(), writer);
                writer.flush();
            }
            try(final ResourceWriter writer = new ResourceWriter(pkgDir.appendSegment("ParserFactory.java"), charset)) {
                parserFactoryTemplate.execute(input, input.coordinates(), writer);
                writer.flush();
            }
        }
    }

}