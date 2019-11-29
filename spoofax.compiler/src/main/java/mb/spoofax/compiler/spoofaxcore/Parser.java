package mb.spoofax.compiler.spoofaxcore;

import com.samskivert.mustache.Template;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.BuilderBase;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.GradleAddDependency;
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
public class Parser {
    private final Template tableTemplate;
    private final Template parserTemplate;
    private final Template factoryTemplate;
    private final ResourceService resourceService;
    private final Charset charset;


    private Parser(Template tableTemplate, Template parserTemplate, Template factoryTemplate, ResourceService resourceService, Charset charset) {
        this.tableTemplate = tableTemplate;
        this.parserTemplate = parserTemplate;
        this.factoryTemplate = factoryTemplate;
        this.resourceService = resourceService;
        this.charset = charset;
    }

    public static Parser fromClassLoaderResources(ResourceService resourceService, Charset charset) {
        final TemplateCompiler templateCompiler = new TemplateCompiler(Parser.class);
        return new Parser(
            templateCompiler.compile("parser/ParseTable.java.mustache"),
            templateCompiler.compile("parser/Parser.java.mustache"),
            templateCompiler.compile("parser/ParserFactory.java.mustache"),
            resourceService,
            charset
        );
    }


    public LanguageProjectOutput compileLanguageProject(Input input) throws IOException {
        final LanguageProjectOutput output = LanguageProjectOutput.builder().fromInput(input).build();
        if(input.classKind().isManualOnly()) return output; // Nothing to generate: return.

        final HierarchicalResource genSourcesJavaDirectory = resourceService.getHierarchicalResource(output.genDirectory());
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

    public AdapterProjectOutput compileAdapterProject(Input input, GradleProject adapterProject) throws IOException {
        return AdapterProjectOutput.builder().build();
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


        @Value.Default default String tableSourceRelPath() {
            return "target/metaborg/sdf.tbl";
        }

        @Value.Default default String tableTargetRelPath() {
            return shared().languageProject().packagePath() + "/" + tableSourceRelPath();
        }


        @Value.Default default ClassKind classKind() {
            return ClassKind.Generated;
        }

        Optional<String> manualParserClass();

        Optional<String> manualFactoryClass();

        @Value.Default default String genTableClass() {
            return shared().classSuffix() + "ParseTable";
        }

        @Value.Derived default String genTableFileName() {
            return genTableClass() + ".java";
        }

        @Value.Default default String genParserClass() {
            return shared().classSuffix() + "Parser";
        }

        @Value.Derived default String genParserFileName() {
            return genParserClass() + ".java";
        }

        @Value.Default default String genFactoryClass() {
            return shared().classSuffix() + "ParserFactory";
        }

        @Value.Derived default String genFactoryFileName() {
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
    public interface LanguageProjectOutput extends Serializable {
        class Builder extends ParserData.LanguageProjectOutput.Builder {
            public Builder fromInput(Input input) {
                final GradleProject languageProject = input.shared().languageProject();
                final ResourcePath genDirectory = languageProject.genSourceSpoofaxJavaDirectory().appendRelativePath(languageProject.packagePath());
                genDirectory(genDirectory);
                genTableFile(genDirectory.appendRelativePath(input.genTableFileName()));
                genParserFile(genDirectory.appendRelativePath(input.genParserFileName()));
                genFactoryFile(genDirectory.appendRelativePath(input.genFactoryFileName()));
                addDependencies(GradleAddDependency.api(input.shared().jsglr1CommonDep()));
                addCopyResources(input.tableSourceRelPath());
                return this;
            }
        }

        static Builder builder() {
            return new Builder();
        }


        ResourcePath genDirectory();

        ResourcePath genTableFile();

        ResourcePath genParserFile();

        ResourcePath genFactoryFile();


        List<GradleAddDependency> dependencies();

        List<String> copyResources();
    }

    @Value.Immutable
    public interface AdapterProjectOutput extends Serializable {
        class Builder extends ParserData.AdapterProjectOutput.Builder {

        }

        static Builder builder() {
            return new Builder();
        }
    }
}
