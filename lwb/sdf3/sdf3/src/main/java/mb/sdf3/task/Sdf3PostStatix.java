package mb.sdf3.task;

import mb.sdf3.Sdf3Scope;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.pie.AstStrategoTransformTaskDef;

import javax.inject.Inject;
import javax.inject.Provider;

@Sdf3Scope
public class Sdf3PostStatix extends AstStrategoTransformTaskDef {
    @Inject public Sdf3PostStatix(Sdf3GetStrategoRuntimeProvider getStrategoRuntimeProvider) {
        super(getStrategoRuntimeProvider, "statix-post");
    }

    @Override public String getId() {
        return Sdf3PostStatix.class.getSimpleName();
    }
}
