package mb.sdf3.task.debug;

import mb.sdf3.Sdf3Scope;
import mb.sdf3.task.Sdf3Desugar;
import mb.sdf3.task.Sdf3GetStrategoRuntimeProvider;
import mb.sdf3.task.Sdf3Parse;
import mb.stratego.common.StrategoRuntime;

import javax.inject.Inject;
import javax.inject.Provider;

@Sdf3Scope
public class Sdf3ShowDesugar extends ShowTaskDef {
    @Inject public Sdf3ShowDesugar(
        Sdf3Parse parse,
        Sdf3Desugar desugar,
        Sdf3Desugar operation, // TODO: don't desugar twice!
        Sdf3GetStrategoRuntimeProvider getStrategoRuntimeProvider
    ) {
        super(parse, desugar, operation, getStrategoRuntimeProvider, "pp-SDF3-string", "desugared");
    }

    @Override public String getId() {
        return getClass().getName();
    }
}
