package mb.tiger;

import mb.constraint.common.ConstraintAnalyzer;
import mb.log.api.LoggerFactory;
import mb.resource.ResourceService;
import mb.stratego.common.StrategoRuntime;

public class TigerConstraintAnalyzer extends ConstraintAnalyzer {
    public TigerConstraintAnalyzer(LoggerFactory loggerFactory, ResourceService resourceService, StrategoRuntime strategoRuntime) {
        super(loggerFactory, resourceService, strategoRuntime, "editor-analyze", false);
    }
}
