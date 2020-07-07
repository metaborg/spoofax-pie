package mb.sdf3.spoofax.task;

import mb.spoofax.core.language.LanguageScope;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.pie.AstStrategoTransformTaskDef;

import javax.inject.Inject;
import javax.inject.Provider;

@LanguageScope
public class Sdf3ToPermissive extends AstStrategoTransformTaskDef {
    @Inject public Sdf3ToPermissive(Provider<StrategoRuntime> strategoRuntimeProvider) {
        super(strategoRuntimeProvider, "module-to-permissive");
    }

    @Override public String getId() {
        return getClass().getName();
    }
}
