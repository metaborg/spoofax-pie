package mb.tiger;

import mb.log.api.LoggerFactory;
import mb.resource.ResourceService;
import mb.spoofax.compiler.interfaces.spoofaxcore.ConstraintAnalyzerFactory;
import mb.stratego.common.StrategoRuntime;

public class TigerConstraintAnalyzerFactory implements ConstraintAnalyzerFactory {
    private final LoggerFactory loggerFactory;
    private final ResourceService resourceService;
    private final StrategoRuntime strategoRuntime;

    public TigerConstraintAnalyzerFactory(LoggerFactory loggerFactory, ResourceService resourceService, StrategoRuntime strategoRuntime) {
        this.loggerFactory = loggerFactory;
        this.resourceService = resourceService;
        this.strategoRuntime = strategoRuntime;
    }

    @Override public TigerConstraintAnalyzer create() {
        return new TigerConstraintAnalyzer(loggerFactory, resourceService, strategoRuntime);
    }
}
