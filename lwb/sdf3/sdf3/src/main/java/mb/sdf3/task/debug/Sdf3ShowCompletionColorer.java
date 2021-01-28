package mb.sdf3.task.debug;

import mb.sdf3.Sdf3Scope;
import mb.sdf3.task.Sdf3Desugar;
import mb.sdf3.task.Sdf3GetStrategoRuntimeProvider;
import mb.sdf3.task.Sdf3Parse;
import mb.sdf3.task.Sdf3ToCompletionColorer;
import mb.stratego.common.StrategoRuntime;

import javax.inject.Inject;
import javax.inject.Provider;

@Sdf3Scope
public class Sdf3ShowCompletionColorer extends ShowTaskDef {
    @Inject public Sdf3ShowCompletionColorer(
        Sdf3Parse parse,
        Sdf3Desugar desugar,
        Sdf3ToCompletionColorer operation,
        Sdf3GetStrategoRuntimeProvider getStrategoRuntimeProvider
    ) {
        super(parse, desugar, operation, getStrategoRuntimeProvider, "pp-esv-to-string", "completion colorer");
    }

    @Override public String getId() {
        return getClass().getName();
    }
}
