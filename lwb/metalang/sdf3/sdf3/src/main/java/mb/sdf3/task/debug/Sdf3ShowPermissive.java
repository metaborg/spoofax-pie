package mb.sdf3.task.debug;

import mb.sdf3.Sdf3Scope;
import mb.sdf3.task.Sdf3Desugar;
import mb.sdf3.task.Sdf3GetStrategoRuntimeProvider;
import mb.sdf3.task.Sdf3Parse;
import mb.sdf3.task.Sdf3ToPermissive;
import mb.stratego.common.StrategoRuntime;

import javax.inject.Inject;
import javax.inject.Provider;

@Sdf3Scope
public class Sdf3ShowPermissive extends ShowTaskDef {
    @Inject public Sdf3ShowPermissive(
        Sdf3Parse parse,
        Sdf3Desugar desugar,
        Sdf3ToPermissive operation,
        Sdf3GetStrategoRuntimeProvider getStrategoRuntimeProvider
    ) {
        super(parse, desugar, operation, getStrategoRuntimeProvider, "pp-SDF3-string", "permissive grammar");
    }

    @Override public String getId() {
        return getClass().getName();
    }
}
