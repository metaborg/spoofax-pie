package mb.sdf3;

import mb.log.api.LoggerFactory;
import mb.resource.ResourceService;
import mb.stratego.common.StrategoRuntimeBuilder;

public class Sdf3ManualStrategoRuntimeBuilderFactory extends Sdf3StrategoRuntimeBuilderFactory {
    @Override
    public StrategoRuntimeBuilder create(LoggerFactory loggerFactory, ResourceService resourceService) {
        final StrategoRuntimeBuilder builder = super.create(loggerFactory, resourceService);
        builder.addLibrary(new Sdf3PrimitiveLibrary());
        return builder;
    }
}
