package mb.tiger;

import mb.resource.ResourceService;
import mb.spoofax.compiler.interfaces.spoofaxcore.ConstraintAnalyzerFactory;

public class TigerConstraintAnalyzerFactory implements ConstraintAnalyzerFactory {
    private final ResourceService resourceService;

    public TigerConstraintAnalyzerFactory(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @Override public TigerConstraintAnalyzer create() {
        return new TigerConstraintAnalyzer(resourceService);
    }
}
