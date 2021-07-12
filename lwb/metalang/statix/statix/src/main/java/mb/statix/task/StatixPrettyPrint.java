package mb.statix.task;

import mb.statix.StatixScope;
import mb.stratego.pie.AstStrategoTransformTaskDef;

import javax.inject.Inject;

@StatixScope
public class StatixPrettyPrint extends AstStrategoTransformTaskDef {
    @Inject public StatixPrettyPrint(StatixGetStrategoRuntimeProvider getStrategoRuntimeProvider) {
        super(getStrategoRuntimeProvider, "pp-StatixLang-string");
    }

    @Override public String getId() {
        return getClass().getName();
    }
}
