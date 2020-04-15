package mb.sdf3;

import mb.log.api.LoggerFactory;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.stratego.common.StrategoRuntimeBuilder;

public class Sdf3ManualStrategoRuntimeBuilderFactory extends Sdf3StrategoRuntimeBuilderFactory {
    public Sdf3ManualStrategoRuntimeBuilderFactory(LoggerFactory loggerFactory, ResourceService resourceService, HierarchicalResource definitionDir) {
        super(loggerFactory, resourceService, definitionDir);
    }

    @Override public StrategoRuntimeBuilder create() {
        final StrategoRuntimeBuilder builder = super.create();
        builder.addLibrary(new Sdf3PrimitiveLibrary());
        return builder;
    }
}
