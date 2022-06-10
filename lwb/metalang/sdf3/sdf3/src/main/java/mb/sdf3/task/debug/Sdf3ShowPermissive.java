package mb.sdf3.task.debug;

import mb.sdf3.Sdf3Scope;
import mb.sdf3.task.Sdf3Desugar;
import mb.sdf3.task.Sdf3GetStrategoRuntimeProvider;
import mb.sdf3.task.Sdf3ToPermissive;
import mb.sdf3.task.spoofax.Sdf3ParseWrapper;

import javax.inject.Inject;

@Sdf3Scope
public class Sdf3ShowPermissive extends ShowTaskDef {
    @Inject public Sdf3ShowPermissive(
        Sdf3ParseWrapper parse,
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
