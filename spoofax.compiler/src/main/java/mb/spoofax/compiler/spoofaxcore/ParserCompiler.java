package mb.spoofax.compiler.spoofaxcore;

import com.samskivert.mustache.Template;
import mb.resource.hierarchical.HierarchicalResource;
import mb.spoofax.compiler.util.ImmutableResourceDeps;
import mb.spoofax.compiler.util.ResourceDeps;
import mb.spoofax.compiler.util.ResourceWriter;
import mb.spoofax.compiler.util.TemplateCompiler;

import java.io.IOException;
import java.nio.charset.Charset;

public class ParserCompiler {
    private final Template parseTableTemplate;
    private final Template parserTemplate;
    private final Template parserFactoryTemplate;

    private ParserCompiler(Template parseTableTemplate, Template parserTemplate, Template parserFactoryTemplate) {
        this.parseTableTemplate = parseTableTemplate;
        this.parserTemplate = parserTemplate;
        this.parserFactoryTemplate = parserFactoryTemplate;
    }

    public static ParserCompiler fromClassLoaderResources() {
        final TemplateCompiler templateCompiler = new TemplateCompiler(ParserCompiler.class);
        return new ParserCompiler(
            templateCompiler.compile("ParseTable.java.mustache"),
            templateCompiler.compile("Parser.java.mustache"),
            templateCompiler.compile("ParserFactory.java.mustache")
        );
    }


    public ResourceDeps compile(ParserCompilerInput input, HierarchicalResource langDir, Charset charset) throws IOException {
        final ImmutableResourceDeps.Builder deps = ImmutableResourceDeps.builder();
        if(input.classKind().isManualOnly()) return deps.build(); // Nothing to generate: return.

        final HierarchicalResource langPkgDir = getPackageDir(input, langDir);
        langPkgDir.createDirectory(true);

        final HierarchicalResource parseTable = getGenParseTableFile(input, langPkgDir);
        try(final ResourceWriter writer = new ResourceWriter(parseTable, charset)) {
            parseTableTemplate.execute(input, writer);
            writer.flush();
        }

        final HierarchicalResource parser = getParserFile(input, langPkgDir);
        try(final ResourceWriter writer = new ResourceWriter(parser, charset)) {
            parserTemplate.execute(input, writer);
            writer.flush();
        }

        final HierarchicalResource parserFactory = getParserFactoryFile(input, langPkgDir);
        try(final ResourceWriter writer = new ResourceWriter(parserFactory, charset)) {
            parserFactoryTemplate.execute(input, writer);
            writer.flush();
        }

        // TODO: generate parse task in .spoofax project

        return deps.addProvidedResources(parseTable, parser, parserFactory).build();
    }


    public HierarchicalResource getPackageDir(ParserCompilerInput input, HierarchicalResource baseDir) {
        return baseDir.appendRelativePath(input.languageProject().packagePath());
    }

    public HierarchicalResource getGenParseTableFile(ParserCompilerInput input, HierarchicalResource packageDir) {
        return packageDir.appendSegment(input.genTablePath());
    }

    public HierarchicalResource getParserFile(ParserCompilerInput input, HierarchicalResource packageDir) {
        return packageDir.appendSegment(input.genParserPath());
    }

    public HierarchicalResource getParserFactoryFile(ParserCompilerInput input, HierarchicalResource packageDir) {
        return packageDir.appendSegment(input.genParserFactoryPath());
    }
}
