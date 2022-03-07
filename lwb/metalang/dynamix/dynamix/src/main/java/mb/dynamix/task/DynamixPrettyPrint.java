package mb.dynamix.task;

import mb.dynamix.DynamixScope;
import mb.stratego.pie.AstStrategoTransformTaskDef;

import javax.inject.Inject;

@DynamixScope
public class DynamixPrettyPrint extends AstStrategoTransformTaskDef {
    @Inject public DynamixPrettyPrint(DynamixGetStrategoRuntimeProvider getStrategoRuntimeProvider) {
        super(getStrategoRuntimeProvider, "pp-dynamix-string");
    }

    @Override public String getId() {
        return getClass().getName();
    }
}
