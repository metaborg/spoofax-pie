package mb.spoofax.compiler.interfaces.spoofaxcore;

import mb.log.api.LoggerFactory;
import mb.resource.ResourceService;
import mb.stratego.common.StrategoRuntimeBuilder;

public interface StrategoRuntimeBuilderFactory {
    StrategoRuntimeBuilder create(LoggerFactory loggerFactory, ResourceService resourceService);
}
