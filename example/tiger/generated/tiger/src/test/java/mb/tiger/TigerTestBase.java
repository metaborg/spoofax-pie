package mb.tiger;

import mb.log.api.LoggerFactory;
import mb.log.noop.NoopLoggerFactory;
import mb.resource.DefaultResourceService;
import mb.resource.DummyResourceRegistry;
import mb.resource.ResourceService;
import mb.resource.classloader.ClassLoaderResourceRegistry;
import mb.resource.fs.FSResourceRegistry;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.url.URLResourceRegistry;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.common.StrategoRuntimeBuilder;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.TermFactory;

class TigerTestBase {
    protected static final String qualifier = "test";

    protected final LoggerFactory loggerFactory = new NoopLoggerFactory();

    protected final ClassLoaderResourceRegistry classLoaderResourceRegistry = TigerClassloaderResources.createClassLoaderResourceRegistry();
    protected final HierarchicalResource definitionDir = TigerClassloaderResources.createDefinitionDir(classLoaderResourceRegistry);
    protected final ResourceService resourceService = new DefaultResourceService(new DummyResourceRegistry(qualifier), new FSResourceRegistry(), new URLResourceRegistry(), classLoaderResourceRegistry);

    protected final TigerParser parser = new TigerParserFactory(definitionDir).create();
    protected final TigerStyler styler = new TigerStylerFactory(loggerFactory, definitionDir).create();
    protected final StrategoRuntimeBuilder strategoRuntimeBuilder = new TigerStrategoRuntimeBuilderFactory(loggerFactory, resourceService, definitionDir).create();
    protected final StrategoRuntime strategoRuntime = strategoRuntimeBuilder.build();
    protected final TigerConstraintAnalyzer analyzer = new TigerConstraintAnalyzerFactory(loggerFactory, resourceService, strategoRuntime).create();

    protected final ITermFactory termFactory = new TermFactory();
}
