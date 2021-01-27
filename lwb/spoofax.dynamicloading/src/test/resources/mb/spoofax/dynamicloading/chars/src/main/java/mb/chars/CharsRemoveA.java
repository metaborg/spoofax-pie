package mb.chars;

import mb.stratego.pie.AstStrategoTransformTaskDef;

import javax.inject.Inject;

public class CharsRemoveA extends AstStrategoTransformTaskDef {
    @Inject public CharsRemoveA(mb.chars.task.CharsGetStrategoRuntimeProvider getStrategoRuntimeProvider) {
        super(getStrategoRuntimeProvider, "remove-a");
    }

    @Override public String getId() {
        return getClass().getName();
    }
}
