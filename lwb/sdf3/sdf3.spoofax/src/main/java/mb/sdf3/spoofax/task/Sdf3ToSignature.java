package mb.sdf3.spoofax.task;

import mb.constraint.pie.ConstraintAnalyzeMultiTaskDef;
import mb.sdf3.spoofax.Sdf3Scope;
import mb.sdf3.spoofax.task.util.AnalyzedStrategoTransformTaskDef;
import mb.stratego.common.StrategoRuntime;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.core.VarScope;
import org.strategoxt.lang.InteropSDefT;

import javax.inject.Inject;
import javax.inject.Provider;

@Sdf3Scope
public class Sdf3ToSignature extends AnalyzedStrategoTransformTaskDef {
    @Inject public Sdf3ToSignature(Provider<StrategoRuntime> strategoRuntimeProvider) {
        super(strategoRuntimeProvider, "desugar-templates", "module-to-sig");
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override protected StrategoRuntime getStrategoRuntime(ConstraintAnalyzeMultiTaskDef.SingleFileOutput input) {
        final StrategoRuntime strategoRuntime = super.getStrategoRuntime(input);
        // HACK: override get-sdf3-type to statix-get-type.
        final IContext context = strategoRuntime.getHybridInterpreter().getContext();
        try {
            context.getVarScope().getParent().addSVar("get_sdf3_type_0_0", context.lookupSVar("statix_get_type_0_0"));
        } catch(InterpreterException e) {
            throw new RuntimeException("Could not override 'get-sdf3-type' to 'statix-get-type'", e);
        }
        return strategoRuntime;
    }
}
