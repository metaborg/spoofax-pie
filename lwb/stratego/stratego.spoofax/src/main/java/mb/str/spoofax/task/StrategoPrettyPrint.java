package mb.str.spoofax.task;

import mb.str.spoofax.StrategoScope;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.pie.AstStrategoTransformTaskDef;

import javax.inject.Inject;
import javax.inject.Provider;

@StrategoScope
public class StrategoPrettyPrint extends AstStrategoTransformTaskDef {
    @Inject public StrategoPrettyPrint(Provider<StrategoRuntime> strategoRuntimeProvider) {
        super(strategoRuntimeProvider, "pp-stratego-string");
    }

    @Override public String getId() {
        return getClass().getName();
    }
}
