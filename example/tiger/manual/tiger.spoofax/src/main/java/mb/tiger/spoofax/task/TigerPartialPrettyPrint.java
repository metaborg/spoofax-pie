package mb.tiger.spoofax.task;

import mb.stratego.pie.AstStrategoTransformTaskDef;
import mb.tiger.spoofax.TigerScope;

import javax.inject.Inject;

// TODO: Make this a template
@TigerScope
public class TigerPartialPrettyPrint extends AstStrategoTransformTaskDef {
    @Inject public TigerPartialPrettyPrint(TigerGetStrategoRuntimeProvider getStrategoRuntimeProvider) {
        super(getStrategoRuntimeProvider, "pp-partial-Tiger-string");
    }

    @Override public String getId() {
        return getClass().getName();
    }
}
