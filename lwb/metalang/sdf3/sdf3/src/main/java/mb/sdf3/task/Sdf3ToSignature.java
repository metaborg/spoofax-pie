package mb.sdf3.task;

import mb.common.result.Result;
import mb.common.util.ListView;
import mb.constraint.pie.ConstraintAnalyzeMultiTaskDef;
import mb.pie.api.Interactivity;
import mb.pie.api.Supplier;
import mb.sdf3.Sdf3Scope;
import mb.sdf3.task.util.AnalyzedStrategoTransformTaskDef;
import mb.stratego.common.Strategy;
import org.spoofax.interpreter.terms.ITermFactory;

import javax.inject.Inject;
import java.util.Set;

@Sdf3Scope
public class Sdf3ToSignature extends AnalyzedStrategoTransformTaskDef {
    @Inject
    public Sdf3ToSignature(Sdf3GetStrategoRuntimeProvider getStrategoRuntimeProvider, ITermFactory termFactory) {
        super(getStrategoRuntimeProvider, Strategy.strategy("desugar-templates"), Strategy.strategy("module-to-sig", ListView.of(), ListView.of(termFactory.makeString("2"))));
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public boolean shouldExecWhenAffected(Supplier<? extends Result<ConstraintAnalyzeMultiTaskDef.SingleFileOutput, ?>> input, Set<?> tags) {
        return tags.isEmpty() || tags.contains(Interactivity.NonInteractive);
    }
}
