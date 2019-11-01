package mb.spoofax.compiler.spoofaxcore;

import com.samskivert.mustache.Template;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.spoofax.compiler.util.ImmutableResourceDependencies;
import mb.spoofax.compiler.util.ResourceDependencies;
import mb.spoofax.compiler.util.ResourceWriter;
import mb.spoofax.compiler.util.TemplateCompiler;

import java.io.IOException;
import java.nio.charset.Charset;

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
            templateCompiler.compile("ParseTable.java.mustache"),
            templateCompiler.compile("Parser.java.mustache"),
            templateCompiler.compile("ParserFactory.java.mustache")
        );
    }


    public ResourceDependencies compile(ParserCompilerInput input, Charset charset) throws IOException {
        final ImmutableResourceDependencies.Builder deps = ImmutableResourceDependencies.builder();
        if(input.classKind().isManualOnly()) return deps.build(); // Nothing to generate: return.

        final HierarchicalResource packageDirectory = getPackageDirectory(input);
        packageDirectory.ensureDirectoryExists();

        final HierarchicalResource parseTableFile = getGenParseTableFile(input);
        try(final ResourceWriter writer = new ResourceWriter(parseTableFile, charset)) {
            parseTableTemplate.execute(input, writer);
            writer.flush();
        }

        final HierarchicalResource parserFile = getParserFile(input);
        try(final ResourceWriter writer = new ResourceWriter(parserFile, charset)) {
            parserTemplate.execute(input, writer);
            writer.flush();
        }

        final HierarchicalResource parserFactoryFile = getParserFactoryFile(input);
        try(final ResourceWriter writer = new ResourceWriter(parserFactoryFile, charset)) {
            parserFactoryTemplate.execute(input, writer);
            writer.flush();
        }

        // TODO: generate parse task in .spoofax project

        return deps.addProvidedResources(parseTableFile, parserFile, parserFactoryFile).build();
    }


    // TODO: remove following methods, as they are leaking the internal workings of this compiler.

    public HierarchicalResource getJavaSourceDirectory(ParserCompilerInput input) {
        return resourceService.getHierarchicalResource(input.languageProject().directory().appendRelativePath("src/main/java"));
    }

    public HierarchicalResource getPackageDirectory(ParserCompilerInput input) {
        return getJavaSourceDirectory(input).appendRelativePath(input.languageProject().packagePath());
    }

    public HierarchicalResource getGenParseTableFile(ParserCompilerInput input) {
        return getPackageDirectory(input).appendSegment(input.genTablePath());
    }

    public HierarchicalResource getParserFile(ParserCompilerInput input) {
        return getPackageDirectory(input).appendSegment(input.genParserPath());
    }

    public HierarchicalResource getParserFactoryFile(ParserCompilerInput input) {
        return getPackageDirectory(input).appendSegment(input.genParserFactoryPath());
    }
}
