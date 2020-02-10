package mb.tiger;

import mb.constraint.common.ConstraintAnalyzer;
import mb.stratego.common.StrategoRuntime;

public class TigerConstraintAnalyzer extends ConstraintAnalyzer {
    public TigerConstraintAnalyzer(StrategoRuntime strategoRuntime) {
        super(strategoRuntime, "editor-analyze", false);
    }
}
