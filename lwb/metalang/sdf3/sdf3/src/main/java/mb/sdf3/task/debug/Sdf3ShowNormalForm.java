package mb.sdf3.task.debug;

import mb.sdf3.Sdf3Scope;
import mb.sdf3.task.Sdf3Desugar;
import mb.sdf3.task.Sdf3GetStrategoRuntimeProvider;
import mb.sdf3.task.Sdf3ToNormalForm;
import mb.sdf3.task.spoofax.Sdf3ParseWrapper;

import javax.inject.Inject;

@Sdf3Scope
public class Sdf3ShowNormalForm extends ShowTaskDef {
    @Inject public Sdf3ShowNormalForm(
        Sdf3ParseWrapper parse,
        Sdf3Desugar desugar,
        Sdf3ToNormalForm operation,
        Sdf3GetStrategoRuntimeProvider getStrategoRuntimeProvider
    ) {
        super(parse, desugar, operation, getStrategoRuntimeProvider, "pp-SDF3-string", "normal-form");
    }

    @Override public String getId() {
        return getClass().getName();
    }
}
