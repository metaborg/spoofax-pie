package mb.sdf3.task.debug;

import mb.sdf3.Sdf3Scope;
import mb.sdf3.task.Sdf3Desugar;
import mb.sdf3.task.Sdf3Parse;
import mb.sdf3.task.Sdf3ToCompletion;
import mb.stratego.common.StrategoRuntime;

import javax.inject.Inject;
import javax.inject.Provider;

@Sdf3Scope
public class Sdf3ShowCompletion extends ShowTaskDef {
    @Inject public Sdf3ShowCompletion(
        Sdf3Parse parse,
        Sdf3Desugar desugar,
        Sdf3ToCompletion operation,
        Provider<StrategoRuntime> strategoRuntimeProvider
    ) {
        super(parse, desugar, operation, strategoRuntimeProvider, "pp-SDF3-string", "completion insertions");
    }

    @Override public String getId() {
        return getClass().getName();
    }
}
