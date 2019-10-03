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


    public ResourceDeps compile(ParserInput input, HierarchicalResource baseDir, Charset charset) throws IOException {
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


    public HierarchicalResource getPackageDir(ParserInput input, HierarchicalResource baseDir) {
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