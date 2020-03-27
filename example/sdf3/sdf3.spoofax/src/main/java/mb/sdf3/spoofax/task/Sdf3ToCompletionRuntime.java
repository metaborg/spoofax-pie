package mb.sdf3.spoofax.task;

import mb.spoofax.core.language.LanguageScope;
import mb.stratego.common.StrategoRuntime;

import javax.inject.Inject;
import javax.inject.Provider;

@LanguageScope
public class Sdf3ToCompletionRuntime extends StrategoTransformTaskDef {
    @Inject public Sdf3ToCompletionRuntime(Provider<StrategoRuntime> strategoRuntimeProvider) {
        super(strategoRuntimeProvider, "module-to-new-cmp");
    }

    @Override public String getId() {
        return getClass().getName();
    }
}

