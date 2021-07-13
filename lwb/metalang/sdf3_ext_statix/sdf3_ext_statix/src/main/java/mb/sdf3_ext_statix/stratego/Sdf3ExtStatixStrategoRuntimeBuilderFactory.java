package mb.sdf3_ext_statix.stratego;

import mb.log.api.LoggerFactory;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.stratego.common.StrategoRuntimeBuilder;

public class Sdf3ExtStatixStrategoRuntimeBuilderFactory extends BaseSdf3ExtStatixStrategoRuntimeBuilderFactory {
    public Sdf3ExtStatixStrategoRuntimeBuilderFactory(LoggerFactory loggerFactory, ResourceService resourceService, HierarchicalResource definitionDir) {
        super(loggerFactory, resourceService, definitionDir);
    }

    @Override public StrategoRuntimeBuilder create() {
        final StrategoRuntimeBuilder builder = super.create();
        builder.addLibrary(new Sdf3ExtStatixPrimitiveLibrary());
        return builder;
    }
}
