package mb.sdf3.spoofax.task.debug;

import mb.sdf3.spoofax.Sdf3Scope;
import mb.sdf3.spoofax.task.Sdf3Desugar;
import mb.sdf3.spoofax.task.Sdf3Parse;
import mb.sdf3.spoofax.task.Sdf3ToCompletionColorer;
import mb.stratego.common.StrategoRuntime;

import javax.inject.Inject;
import javax.inject.Provider;

@Sdf3Scope
public class Sdf3ShowCompletionColorer extends ShowTaskDef {
    @Inject public Sdf3ShowCompletionColorer(
        Sdf3Parse parse,
        Sdf3Desugar desugar,
        Sdf3ToCompletionColorer operation,
        Provider<StrategoRuntime> strategoRuntimeProvider
    ) {
        super(parse, desugar, operation, strategoRuntimeProvider, "pp-esv-to-string", "completion colorer");
    }

    @Override public String getId() {
        return getClass().getName();
    }
}
