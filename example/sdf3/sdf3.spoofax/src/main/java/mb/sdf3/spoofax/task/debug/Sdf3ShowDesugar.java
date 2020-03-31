package mb.sdf3.spoofax.task.debug;

import mb.sdf3.spoofax.task.Sdf3Desugar;
import mb.sdf3.spoofax.task.Sdf3Parse;
import mb.sdf3.spoofax.task.Sdf3ToCompletion;
import mb.spoofax.core.language.LanguageScope;
import mb.stratego.common.StrategoRuntime;

import javax.inject.Inject;
import javax.inject.Provider;

@LanguageScope
public class Sdf3ShowDesugar extends ShowTaskDef {
    @Inject public Sdf3ShowDesugar(
        Sdf3Parse parse,
        Sdf3Desugar desugar,
        Sdf3Desugar operation, // TODO: don't desugar twice!
        Provider<StrategoRuntime> strategoRuntimeProvider
    ) {
        super(parse, desugar, operation, strategoRuntimeProvider, "pp-SDF3-string", "desugared");
    }

    @Override public String getId() {
        return getClass().getName();
    }
}
