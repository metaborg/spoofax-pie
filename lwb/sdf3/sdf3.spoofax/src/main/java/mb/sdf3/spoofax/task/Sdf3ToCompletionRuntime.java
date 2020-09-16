package mb.sdf3.spoofax.task;

import mb.spoofax.core.language.LanguageScope;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.pie.AstStrategoTransformTaskDef;

import javax.inject.Inject;
import javax.inject.Provider;

@LanguageScope
public class Sdf3ToCompletionRuntime extends AstStrategoTransformTaskDef {
    @Inject public Sdf3ToCompletionRuntime(Provider<StrategoRuntime> strategoRuntimeProvider) {
        super(strategoRuntimeProvider, "module-to-new-cmp");
    }

    @Override public String getId() {
        return getClass().getName();
    }
}

