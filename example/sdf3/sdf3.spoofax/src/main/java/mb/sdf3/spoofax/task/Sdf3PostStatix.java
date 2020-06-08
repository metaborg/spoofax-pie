package mb.sdf3.spoofax.task;

import mb.stratego.common.StrategoRuntime;

import javax.inject.Inject;
import javax.inject.Provider;

public class Sdf3PostStatix extends StrategoTransformTaskDef {

    @Inject public Sdf3PostStatix(Provider<StrategoRuntime> strategoRuntimeProvider) {
        super(strategoRuntimeProvider, "statix-post");
    }

    @Override public String getId() {
        return Sdf3PostStatix.class.getSimpleName();
    }
}
