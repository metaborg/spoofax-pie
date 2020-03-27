package mb.sdf3.spoofax.task;

import mb.spoofax.core.language.LanguageScope;
import mb.stratego.common.StrategoRuntime;

import javax.inject.Inject;
import javax.inject.Provider;

@LanguageScope
public class Sdf3DesugarTemplates extends StrategoTransformTaskDef {
    @Inject public Sdf3DesugarTemplates(Provider<StrategoRuntime> strategoRuntimeProvider) {
        super(strategoRuntimeProvider, "desugar-templates");
    }

    @Override public String getId() {
        return getClass().getName();
    }
}
