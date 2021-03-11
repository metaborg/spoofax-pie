package mb.sdf3.task;

import mb.sdf3.Sdf3Scope;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.pie.AstStrategoTransformTaskDef;

import javax.inject.Inject;
import javax.inject.Provider;

@Sdf3Scope
public class Sdf3PreStatix extends AstStrategoTransformTaskDef {
    @Inject public Sdf3PreStatix(Sdf3GetStrategoRuntimeProvider getStrategoRuntimeProvider) {
        super(getStrategoRuntimeProvider, "statix-pre");
    }

    @Override public String getId() {
        return Sdf3PreStatix.class.getSimpleName();
    }
}
