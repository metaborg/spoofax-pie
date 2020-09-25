package mb.sdf3.spoofax.task;

import mb.sdf3.spoofax.Sdf3Scope;
import mb.sdf3.spoofax.task.util.AnalyzedStrategoTransformTaskDef;
import mb.stratego.common.StrategoRuntime;

import javax.inject.Inject;
import javax.inject.Provider;

@Sdf3Scope
public class Sdf3ToSignature extends AnalyzedStrategoTransformTaskDef {
    @Inject public Sdf3ToSignature(Provider<StrategoRuntime> strategoRuntimeProvider) {
        super(strategoRuntimeProvider, "desugar-templates", "module-to-sig");
    }

    @Override public String getId() {
        return getClass().getName();
    }
}
