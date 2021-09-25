package mb.tiger;

import mb.constraint.common.stratego.ConstraintPrimitiveLibrary;
import mb.log.api.LoggerFactory;
import mb.nabl2.common.NaBL2PrimitiveLibrary;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.spoofax.compiler.interfaces.spoofaxcore.StrategoRuntimeBuilderFactory;
import mb.stratego.common.StrategoRuntimeBuilder;

public class TigerStrategoRuntimeBuilderFactory implements StrategoRuntimeBuilderFactory {
    private final LoggerFactory loggerFactory;
    private final ResourceService resourceService;
    private final HierarchicalResource definitionDir;

    public TigerStrategoRuntimeBuilderFactory(LoggerFactory loggerFactory, ResourceService resourceService, HierarchicalResource definitionDir) {
        this.loggerFactory = loggerFactory;
        this.resourceService = resourceService;
        this.definitionDir = definitionDir;
    }

    @Override
    public StrategoRuntimeBuilder create() {
        final StrategoRuntimeBuilder builder = new StrategoRuntimeBuilder(loggerFactory, resourceService, definitionDir);
        builder.addInteropRegistererByReflection("org.metaborg.lang.tiger.trans.InteropRegisterer");
        builder.addInteropRegistererByReflection("org.metaborg.lang.tiger.strategies.InteropRegisterer");
        builder.withJarParentClassLoader(TigerStrategoRuntimeBuilderFactory.class.getClassLoader());
        builder.addLibrary(new NaBL2PrimitiveLibrary());
        builder.addLibrary(new ConstraintPrimitiveLibrary(resourceService));
        return builder;
    }
}
