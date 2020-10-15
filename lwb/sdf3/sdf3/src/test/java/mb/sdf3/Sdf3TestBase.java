package mb.sdf3;

import mb.log.api.LoggerFactory;
import mb.log.slf4j.SLF4JLoggerFactory;
import mb.resource.DefaultResourceKey;
import mb.resource.DefaultResourceService;
import mb.resource.DummyResourceRegistry;
import mb.resource.ResourceKey;
import mb.resource.ResourceService;
import mb.resource.classloader.ClassLoaderResourceRegistry;
import mb.resource.fs.FSResourceRegistry;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.url.URLResourceRegistry;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.common.StrategoRuntimeBuilder;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.TermFactory;

class Sdf3TestBase {
    protected static final String qualifier = "test";

    protected final LoggerFactory loggerFactory = new SLF4JLoggerFactory();

    protected final ClassLoaderResourceRegistry classLoaderResourceRegistry = Sdf3ClassloaderResources.createClassLoaderResourceRegistry();
    protected final HierarchicalResource definitionDir = Sdf3ClassloaderResources.createDefinitionDir(classLoaderResourceRegistry);
    protected final ResourceService resourceService = new DefaultResourceService(new DummyResourceRegistry(qualifier), new FSResourceRegistry(), new URLResourceRegistry(), classLoaderResourceRegistry);

    protected final Sdf3Parser parser = new Sdf3ParserFactory(definitionDir).create();
    protected final String startSymbol = "Module";
    protected final Sdf3Styler styler = new Sdf3StylerFactory(loggerFactory, definitionDir).create();
    protected final StrategoRuntimeBuilder strategoRuntimeBuilder = new Sdf3StrategoRuntimeBuilderFactory(loggerFactory, resourceService, definitionDir).create();
    protected final StrategoRuntime strategoRuntime = strategoRuntimeBuilder.build();
    protected final Sdf3ConstraintAnalyzer analyzer = new Sdf3ConstraintAnalyzerFactory(resourceService).create();
    protected final ResourceKey rootKey = new DefaultResourceKey(qualifier, "root");

    protected final ITermFactory termFactory = new TermFactory();
}
