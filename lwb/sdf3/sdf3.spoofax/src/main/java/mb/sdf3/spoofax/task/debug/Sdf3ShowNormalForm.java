package mb.sdf3.spoofax.task.debug;

import mb.sdf3.spoofax.Sdf3Scope;
import mb.sdf3.spoofax.task.Sdf3Desugar;
import mb.sdf3.spoofax.task.Sdf3Parse;
import mb.sdf3.spoofax.task.Sdf3ToNormalForm;
import mb.stratego.common.StrategoRuntime;

import javax.inject.Inject;
import javax.inject.Provider;

@Sdf3Scope
public class Sdf3ShowNormalForm extends ShowTaskDef {
    @Inject public Sdf3ShowNormalForm(
        Sdf3Parse parse,
        Sdf3Desugar desugar,
        Sdf3ToNormalForm operation,
        Provider<StrategoRuntime> strategoRuntimeProvider
    ) {
        super(parse, desugar, operation, strategoRuntimeProvider, "pp-SDF3-string", "normal-form");
    }

    @Override public String getId() {
        return getClass().getName();
    }
}
