package mb.sdf3.spoofax.task;

import mb.sdf3.spoofax.Sdf3Scope;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.pie.AstStrategoTransformTaskDef;

import javax.inject.Inject;
import javax.inject.Provider;

@Sdf3Scope
public class Sdf3PreStatix extends AstStrategoTransformTaskDef {
    @Inject public Sdf3PreStatix(Provider<StrategoRuntime> strategoRuntimeProvider) {
        super(strategoRuntimeProvider, "statix-pre");
    }

    @Override public String getId() {
        return Sdf3PreStatix.class.getSimpleName();
    }
}
