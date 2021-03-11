package mb.sdf3.task.debug;

import mb.sdf3.Sdf3Scope;
import mb.sdf3.task.Sdf3Desugar;
import mb.sdf3.task.Sdf3GetStrategoRuntimeProvider;
import mb.sdf3.task.Sdf3Parse;
import mb.sdf3.task.Sdf3ToCompletionRuntime;
import mb.stratego.common.StrategoRuntime;

import javax.inject.Inject;
import javax.inject.Provider;

@Sdf3Scope
public class Sdf3ShowCompletionRuntime extends ShowTaskDef {
    @Inject public Sdf3ShowCompletionRuntime(
        Sdf3Parse parse,
        Sdf3Desugar desugar,
        Sdf3ToCompletionRuntime operation,
        Sdf3GetStrategoRuntimeProvider getStrategoRuntimeProvider
    ) {
        super(parse, desugar, operation, getStrategoRuntimeProvider, "pp-stratego-string", "completion runtime");
    }

    @Override public String getId() {
        return getClass().getName();
    }
}
