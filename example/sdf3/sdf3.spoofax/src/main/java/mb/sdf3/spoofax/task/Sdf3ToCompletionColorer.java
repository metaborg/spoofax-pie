package mb.sdf3.spoofax.task;

import mb.spoofax.core.language.LanguageScope;
import mb.stratego.common.StrategoRuntime;

import javax.inject.Inject;
import javax.inject.Provider;

@LanguageScope
public class Sdf3ToCompletionColorer extends StrategoTransformTaskDef {
    @Inject public Sdf3ToCompletionColorer(Provider<StrategoRuntime> strategoRuntimeProvider) {
        super(strategoRuntimeProvider, "module-to-cmp-colorer");
    }

    @Override public String getId() {
        return getClass().getName();
    }
}
