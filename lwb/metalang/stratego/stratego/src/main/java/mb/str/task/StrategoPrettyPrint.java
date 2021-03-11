package mb.str.task;

import mb.str.StrategoScope;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.pie.AstStrategoTransformTaskDef;

import javax.inject.Inject;
import javax.inject.Provider;

@StrategoScope
public class StrategoPrettyPrint extends AstStrategoTransformTaskDef {
    @Inject public StrategoPrettyPrint(StrategoGetStrategoRuntimeProvider getStrategoRuntimeProvider) {
        super(getStrategoRuntimeProvider, "pp-stratego-string");
    }

    @Override public String getId() {
        return getClass().getName();
    }
}
