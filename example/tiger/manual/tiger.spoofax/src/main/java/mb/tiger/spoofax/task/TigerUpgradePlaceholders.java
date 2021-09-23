package mb.tiger.spoofax.task;

import mb.stratego.pie.AstStrategoTransformTaskDef;
import mb.tiger.spoofax.TigerScope;

import javax.inject.Inject;

// TODO: Make this a template
@TigerScope
public class TigerUpgradePlaceholders extends AstStrategoTransformTaskDef {
    @Inject public TigerUpgradePlaceholders(TigerGetStrategoRuntimeProvider getStrategoRuntimeProvider) {
        super(getStrategoRuntimeProvider, "upgrade-placeholders-Tiger");
    }

    @Override public String getId() {
        return TigerUpgradePlaceholders.class.getSimpleName();
    }
}
