package mb.statix.referenceretention.pie;

import mb.statix.referenceretention.pie.util.AnalyzedStrategoTransformTaskDef;
import mb.stratego.pie.GetStrategoRuntimeProvider;

/**
 * Task definition calling the Stratego transformation 'inline-method-call'
 * on an analyzed AST.
 */
public class InlineMethodCallTaskDef extends AnalyzedStrategoTransformTaskDef {

    public InlineMethodCallTaskDef(
        GetStrategoRuntimeProvider getStrategoRuntimeProvider
    ) {
        super(getStrategoRuntimeProvider, "inline-method-call");
    }

    @Override
    public String getId() {
        return this.getClass().getName();
    }

}
