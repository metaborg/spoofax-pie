package mb.sdf3.task;

import mb.sdf3.Sdf3Scope;
import mb.sdf3.task.util.AnalyzedStrategoTransformTaskDef;
import mb.stratego.common.StrategoRuntime;

import javax.inject.Inject;
import javax.inject.Provider;

@Sdf3Scope
public class Sdf3ToDynsemSignature extends AnalyzedStrategoTransformTaskDef {
    @Inject public Sdf3ToDynsemSignature(Sdf3GetStrategoRuntimeProvider getStrategoRuntimeProvider) {
        super(getStrategoRuntimeProvider, "module-to-ds-sig");
    }

    @Override public String getId() {
        return getClass().getName();
    }
}
