package mb.tiger.spoofax.task;

import mb.stratego.pie.AstStrategoTransformTaskDef;
import mb.tiger.spoofax.TigerScope;

import javax.inject.Inject;

// TODO: Make this a template
@TigerScope
public class TigerPreStatix extends AstStrategoTransformTaskDef {
    @Inject public TigerPreStatix(TigerGetStrategoRuntimeProvider getStrategoRuntimeProvider) {
        super(getStrategoRuntimeProvider, "pre-analyze");
    }

    @Override public String getId() {
        return TigerPreStatix.class.getSimpleName();
    }
}
