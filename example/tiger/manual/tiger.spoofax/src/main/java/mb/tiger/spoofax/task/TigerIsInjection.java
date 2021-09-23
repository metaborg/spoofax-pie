package mb.tiger.spoofax.task;

import mb.stratego.pie.AstStrategoTransformTaskDef;
import mb.tiger.spoofax.TigerScope;

import javax.inject.Inject;

// TODO: Make this a template
@TigerScope
public class TigerIsInjection extends AstStrategoTransformTaskDef {
    @Inject public TigerIsInjection(TigerGetStrategoRuntimeProvider getStrategoRuntimeProvider) {
        super(getStrategoRuntimeProvider, "is-Tiger-inj-cons");
    }

    @Override public String getId() {
        return TigerIsInjection.class.getSimpleName();
    }
}
