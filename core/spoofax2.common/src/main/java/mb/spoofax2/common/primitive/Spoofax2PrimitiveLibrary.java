package mb.spoofax2.common.primitive;

import mb.log.api.LoggerFactory;
import mb.resource.ResourceService;
import org.spoofax.interpreter.library.AbstractStrategoOperatorRegistry;

public class Spoofax2PrimitiveLibrary extends AbstractStrategoOperatorRegistry {
    public Spoofax2PrimitiveLibrary(LoggerFactory loggerFactory, ResourceService resourceService) {
        add(new LanguageComponentPrimitive());

        add(new LanguageResourcesPrimitive(loggerFactory, resourceService));
        add(new ProjectResourcesPrimitive(loggerFactory, resourceService));
        add(new ProjectSrcGenDirectory(loggerFactory, resourceService));
    }

    @Override public String getOperatorRegistryName() {
        return "Spoofax2PrimitiveLibrary";
    }
}
