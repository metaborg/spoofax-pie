package mb.sdf3.spoofax.task.debug;

import mb.sdf3.spoofax.Sdf3Scope;
import mb.sdf3.spoofax.task.Sdf3Desugar;
import mb.sdf3.spoofax.task.Sdf3Parse;
import mb.sdf3.spoofax.task.Sdf3ToPrettyPrinter;
import mb.stratego.common.StrategoRuntime;

import javax.inject.Inject;
import javax.inject.Provider;

@Sdf3Scope
public class Sdf3ShowPrettyPrinter extends ShowTaskDef {
    @Inject public Sdf3ShowPrettyPrinter(
        Sdf3Parse parse,
        Sdf3Desugar desugar,
        Sdf3ToPrettyPrinter operation,
        Provider<StrategoRuntime> strategoRuntimeProvider
    ) {
        super(parse, desugar, operation, strategoRuntimeProvider, "pp-stratego-string", "pretty-printer");
    }

    @Override public String getId() {
        return getClass().getName();
    }
}
