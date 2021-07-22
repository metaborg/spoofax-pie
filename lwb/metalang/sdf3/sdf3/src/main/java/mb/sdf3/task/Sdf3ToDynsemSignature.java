package mb.sdf3.task;

import mb.common.result.Result;
import mb.constraint.pie.ConstraintAnalyzeMultiTaskDef;
import mb.pie.api.Interactivity;
import mb.pie.api.Supplier;
import mb.sdf3.Sdf3Scope;
import mb.sdf3.task.util.AnalyzedStrategoTransformTaskDef;

import javax.inject.Inject;
import java.util.Set;

@Sdf3Scope
public class Sdf3ToDynsemSignature extends AnalyzedStrategoTransformTaskDef {
    @Inject public Sdf3ToDynsemSignature(Sdf3GetStrategoRuntimeProvider getStrategoRuntimeProvider) {
        super(getStrategoRuntimeProvider, "module-to-ds-sig");
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public boolean shouldExecWhenAffected(Supplier<? extends Result<ConstraintAnalyzeMultiTaskDef.SingleFileOutput, ?>> input, Set<?> tags) {
        return tags.isEmpty() || tags.contains(Interactivity.NonInteractive);
    }
}
