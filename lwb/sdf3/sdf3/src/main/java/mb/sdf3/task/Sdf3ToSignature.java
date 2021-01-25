package mb.sdf3.task;

import mb.constraint.pie.ConstraintAnalyzeMultiTaskDef;
import mb.pie.api.ExecContext;
import mb.sdf3.Sdf3Scope;
import mb.sdf3.task.util.AnalyzedStrategoTransformTaskDef;
import mb.stratego.common.StrategoRuntime;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;

import javax.inject.Inject;

@Sdf3Scope
public class Sdf3ToSignature extends AnalyzedStrategoTransformTaskDef {
    @Inject public Sdf3ToSignature(Sdf3GetStrategoRuntimeProvider getStrategoRuntimeProvider) {
        super(getStrategoRuntimeProvider, "desugar-templates", "module-to-sig");
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    protected StrategoRuntime getStrategoRuntime(ExecContext context, ConstraintAnalyzeMultiTaskDef.SingleFileOutput input) {
        final StrategoRuntime strategoRuntime = super.getStrategoRuntime(context, input);
        // HACK: override get-sdf3-type to statix-get-type.
        final IContext strategoContext = strategoRuntime.getHybridInterpreter().getContext();
        try {
            strategoContext.getVarScope().getParent().addSVar("get_sdf3_type_0_0", strategoContext.lookupSVar("statix_get_type_0_0"));
        } catch(InterpreterException e) {
            throw new RuntimeException("Could not override 'get-sdf3-type' to 'statix-get-type'", e);
        }
        return strategoRuntime;
    }
}
