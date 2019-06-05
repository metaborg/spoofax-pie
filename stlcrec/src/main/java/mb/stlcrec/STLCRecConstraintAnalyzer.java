package mb.stlcrec;

import mb.constraint.common.ConstraintAnalyzer;
import mb.stratego.common.StrategoRuntime;

public class STLCRecConstraintAnalyzer extends ConstraintAnalyzer {
    public STLCRecConstraintAnalyzer(StrategoRuntime strategoRuntime) {
        super(strategoRuntime, "editor-analyze", false);
    }
}
