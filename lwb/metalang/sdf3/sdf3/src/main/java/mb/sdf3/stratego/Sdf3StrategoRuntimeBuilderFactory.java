package mb.sdf3.stratego;

import mb.log.api.LoggerFactory;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.stratego.common.StrategoRuntimeBuilder;

public class Sdf3StrategoRuntimeBuilderFactory extends BaseSdf3StrategoRuntimeBuilderFactory {
    public Sdf3StrategoRuntimeBuilderFactory(LoggerFactory loggerFactory, ResourceService resourceService, HierarchicalResource definitionDir) {
        super(loggerFactory, resourceService, definitionDir);
    }

    @Override public StrategoRuntimeBuilder create() {
        final StrategoRuntimeBuilder builder = super.create();
        builder.addLibrary(new Sdf3PrimitiveLibrary());
        return builder;
    }
}
