package mb.sdf3.spoofax.task;

import mb.stratego.common.StrategoRuntime;

import javax.inject.Inject;
import javax.inject.Provider;

public class SDF3PreStatix extends StrategoTransformTaskDef {

    @Inject public SDF3PreStatix(Provider<StrategoRuntime> strategoRuntimeProvider) {
        super(strategoRuntimeProvider, "statix-pre");
    }

    @Override public String getId() {
        return SDF3PreStatix.class.getSimpleName();
    }
}
