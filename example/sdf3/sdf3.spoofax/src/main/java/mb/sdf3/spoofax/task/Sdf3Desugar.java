package mb.sdf3.spoofax.task;

import mb.spoofax.core.language.LanguageScope;
import mb.stratego.common.StrategoRuntime;

import javax.inject.Inject;
import javax.inject.Provider;

@LanguageScope
public class Sdf3Desugar extends StrategoTransformTaskDef {
    @Inject public Sdf3Desugar(Provider<StrategoRuntime> strategoRuntimeProvider) {
        super(strategoRuntimeProvider, "lifting", "desugar-templates");
    }

    @Override public String getId() {
        return getClass().getName();
    }
}
