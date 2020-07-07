package mb.sdf3.spoofax.task;

import mb.stratego.common.StrategoRuntime;
import mb.stratego.pie.AstStrategoTransformTaskDef;

import javax.inject.Inject;
import javax.inject.Provider;

public class Sdf3PreStatix extends AstStrategoTransformTaskDef {
    @Inject public Sdf3PreStatix(Provider<StrategoRuntime> strategoRuntimeProvider) {
        super(strategoRuntimeProvider, "statix-pre");
    }

    @Override public String getId() {
        return Sdf3PreStatix.class.getSimpleName();
    }
}
