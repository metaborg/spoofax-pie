package mb.tiger.spoofax.task;

import mb.stratego.pie.AstStrategoTransformTaskDef;
import mb.tiger.spoofax.TigerScope;

import javax.inject.Inject;

// TODO: Make this a template
@TigerScope
public class TigerPostStatix extends AstStrategoTransformTaskDef {
    @Inject public TigerPostStatix(TigerGetStrategoRuntimeProvider getStrategoRuntimeProvider) {
        super(getStrategoRuntimeProvider, "post-analyze");
    }

    @Override public String getId() {
        return TigerPostStatix.class.getSimpleName();
    }
}
