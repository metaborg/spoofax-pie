package mb.mod;

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

class ModTestBase {
    protected static final String qualifier = "test";

    protected final LoggerFactory loggerFactory = new SLF4JLoggerFactory();

    protected final ClassLoaderResourceRegistry classLoaderResourceRegistry = ModClassloaderResources.createClassLoaderResourceRegistry();
    protected final HierarchicalResource definitionDir = ModClassloaderResources.createDefinitionDir(classLoaderResourceRegistry);
    protected final ResourceService resourceService = new DefaultResourceService(new DummyResourceRegistry(qualifier), new FSResourceRegistry(), new URLResourceRegistry(), classLoaderResourceRegistry);

    protected final ModParser parser = new ModParserFactory(definitionDir).create();
    protected final String startSymbol = "Start";
    protected final ModStyler styler = new ModStylerFactory(loggerFactory, definitionDir).create();
    protected final StrategoRuntimeBuilder strategoRuntimeBuilder = new ModStrategoRuntimeBuilderFactory(loggerFactory, resourceService, definitionDir).create();
    protected final StrategoRuntime strategoRuntime = strategoRuntimeBuilder.build();
    protected final ModConstraintAnalyzer analyzer = new ModConstraintAnalyzerFactory(loggerFactory, resourceService, strategoRuntime).create();
    protected final ResourceKey rootKey = new DefaultResourceKey(qualifier, "root");

    protected final ITermFactory termFactory = new TermFactory();
}
