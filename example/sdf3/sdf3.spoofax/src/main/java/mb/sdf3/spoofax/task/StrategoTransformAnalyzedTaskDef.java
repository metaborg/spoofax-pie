package mb.sdf3.spoofax.task;

import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.stratego.common.StrategoRuntime;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Provider;

public abstract class StrategoTransformAnalyzedTaskDef implements TaskDef<Supplier<SingleFileAnalysisResult>, @Nullable IStrategoTerm> {
    private final Provider<StrategoRuntime> strategoRuntimeProvider;
    private final ListView<String> strategyNames;

    public StrategoTransformAnalyzedTaskDef(Provider<StrategoRuntime> strategoRuntimeProvider, ListView<String> strategyNames) {
        this.strategoRuntimeProvider = strategoRuntimeProvider;
        this.strategyNames = strategyNames;
    }

    public StrategoTransformAnalyzedTaskDef(Provider<StrategoRuntime> strategoRuntimeProvider, String... strategyNames) {
        this.strategoRuntimeProvider = strategoRuntimeProvider;
        this.strategyNames = ListView.of(strategyNames);
    }

    public StrategoTransformAnalyzedTaskDef(Provider<StrategoRuntime> strategoRuntimeProvider, String strategyName) {
        this.strategoRuntimeProvider = strategoRuntimeProvider;
        this.strategyNames = ListView.of(strategyName);
    }

    @Override public @Nullable IStrategoTerm exec(
        ExecContext context,
        Supplier<SingleFileAnalysisResult> supplier
    ) throws Exception {
        final SingleFileAnalysisResult input = context.require(supplier);
        @Nullable IStrategoTerm ast = input.analyzedAst;
        final StrategoRuntime strategoRuntime = strategoRuntimeProvider.get().addContextObject(input.constraintAnalyzerContext);
        for(String strategyName : strategyNames) {
            if(ast == null) return null;
            ast = strategoRuntime.invoke(strategyName, ast);
        }
        return ast;
    }
}
