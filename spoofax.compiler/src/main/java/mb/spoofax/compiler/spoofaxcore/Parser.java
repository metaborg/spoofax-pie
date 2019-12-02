package mb.spoofax.compiler.spoofaxcore;

import com.samskivert.mustache.Template;
import mb.resource.ResourceService;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.ClassInfo;
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

        resourceService.getHierarchicalResource(input.languageProjectGenDirectory()).ensureDirectoryExists();

        try(final ResourceWriter writer = new ResourceWriter(resourceService.getHierarchicalResource(input.genTableFile()).createParents(), charset)) {
            tableTemplate.execute(input, writer);
            writer.flush();
        }

        try(final ResourceWriter writer = new ResourceWriter(resourceService.getHierarchicalResource(input.parserFile()).createParents(), charset)) {
            parserTemplate.execute(input, writer);
            writer.flush();
        }

        try(final ResourceWriter writer = new ResourceWriter(resourceService.getHierarchicalResource(input.factoryFile()).createParents(), charset)) {
            factoryTemplate.execute(input, writer);
            writer.flush();
        }

        return output;
    }

    public AdapterProjectOutput compileAdapterProject(Input input) throws IOException {
        final AdapterProjectOutput output = AdapterProjectOutput.builder().fromInput(input).build();
        if(input.classKind().isManualOnly()) return output; // Nothing to generate: return.

        // TODO: parse task

        // TODO: tokenize task

        // TODO: parser service providers

        // TODO: task providers

        return output;
    }


    @Value.Immutable
    public interface Input extends Serializable {
        class Builder extends ParserData.Input.Builder {}

        static Builder builder() {
            return new Builder();
        }


        Shared shared();


        /// Parse table source file (to copy from), and destination file

        @Value.Default default String tableSourceRelPath() {
            return "target/metaborg/sdf.tbl";
        }

        @Value.Default default String tableTargetRelPath() {
            return shared().languageProject().packagePath() + "/" + tableSourceRelPath();
        }


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() {
            return ClassKind.Generated;
        }


        /// Language project classes

        @Value.Derived default ResourcePath languageProjectGenDirectory() {
            return shared().languageProject().genSourceSpoofaxJavaDirectory();
        }


        @Value.Default default ClassInfo genTableClass() {
            return ClassInfo.of(shared().languageProject().packageId(), shared().classSuffix() + "ParseTable");
        }

        default ResourcePath genTableFile() {
            return genTableClass().file(languageProjectGenDirectory());
        }


        @Value.Default default ClassInfo genParserClass() {
            return ClassInfo.of(shared().languageProject().packageId(), shared().classSuffix() + "Parser");
        }

        Optional<ClassInfo> manualParserClass();

        default ClassInfo parserClass() {
            if(classKind().isManual() && manualParserClass().isPresent()) {
                return manualParserClass().get();
            }
            return genParserClass();
        }

        default ResourcePath parserFile() {
            return parserClass().file(languageProjectGenDirectory());
        }


        @Value.Default default ClassInfo genFactoryClass() {
            return ClassInfo.of(shared().languageProject().packageId(), shared().classSuffix() + "ParserFactory");
        }

        Optional<ClassInfo> manualFactoryClass();

        default ClassInfo factoryClass() {
            if(classKind().isManual() && manualFactoryClass().isPresent()) {
                return manualFactoryClass().get();
            }
            return genFactoryClass();
        }

        default ResourcePath factoryFile() {
            return factoryClass().file(languageProjectGenDirectory());
        }


        /// Adapter project classes

        @Value.Derived default ResourcePath taskdefGenDirectory() {
            final GradleProject adapterProject = shared().adapterProject();
            return adapterProject.genSourceSpoofaxJavaDirectory().appendRelativePath(adapterProject.packagePath() + "/taskdef");
        }


        @Value.Default default String genParseTaskDefClass() {
            return shared().classSuffix() + "Parser";
        }

        @Value.Derived default String genParseTaskDefFileName() {
            return genParseTaskDefClass() + ".java";
        }

        @Value.Derived default ResourcePath genParseTaskDefFile() {
            return taskdefGenDirectory().appendSegment(genParseTaskDefFileName());
        }

        Optional<String> manualParseTaskDefClass();

        @Value.Derived default String parseTaskDefClass() {
            if(classKind().isManual() && manualParseTaskDefClass().isPresent()) {
                return manualParseTaskDefClass().get();
            }
            return genParseTaskDefClass();
        }


        @Value.Default default String genTokenizeTaskDefClass() {
            return shared().classSuffix() + "Parser";
        }

        @Value.Derived default String genTokenizeTaskDefFileName() {
            return genTokenizeTaskDefClass() + ".java";
        }

        @Value.Derived default ResourcePath genTokenizeTaskDefFile() {
            return taskdefGenDirectory().appendSegment(genTokenizeTaskDefFileName());
        }

        Optional<String> manualTokenizeTaskDefClass();

        @Value.Derived default String tokenizeTaskDefClass() {
            if(classKind().isManual() && manualTokenizeTaskDefClass().isPresent()) {
                return manualTokenizeTaskDefClass().get();
            }
            return genTokenizeTaskDefClass();
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
                addDependencies(GradleConfiguredDependency.api(input.shared().jsglr1CommonDep()));
                addCopyResources(input.tableSourceRelPath());
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
        class Builder extends ParserData.AdapterProjectOutput.Builder {
            public Builder fromInput(Input input) {
                factoryClass(input.factoryClass());
                parserClass(input.parserClass());
                parseTaskClass(input.parseTaskDefClass());
                tokenizeTaskClass(input.tokenizeTaskDefClass());
                return this;
            }
        }

        static Builder builder() {
            return new Builder();
        }


        ClassInfo factoryClass();

        ClassInfo parserClass();

        String parseTaskClass();

        String tokenizeTaskClass();
    }
}
