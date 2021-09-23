package mb.tiger.spoofax.task;

import mb.stratego.pie.AstStrategoTransformTaskDef;
import mb.tiger.spoofax.TigerScope;

import javax.inject.Inject;

// TODO: Make this a template
@TigerScope
public class TigerDowngradePlaceholders extends AstStrategoTransformTaskDef {
    @Inject public TigerDowngradePlaceholders(TigerGetStrategoRuntimeProvider getStrategoRuntimeProvider) {
        super(getStrategoRuntimeProvider, "downgrade-placeholders-Tiger");
    }

    @Override public String getId() {
        return TigerDowngradePlaceholders.class.getSimpleName();
    }
}
