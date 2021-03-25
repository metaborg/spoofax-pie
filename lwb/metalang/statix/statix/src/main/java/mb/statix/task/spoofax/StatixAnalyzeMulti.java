package mb.statix.task.spoofax;

import mb.common.message.KeyedMessages;
import mb.common.message.Message;
import mb.common.option.Option;
import mb.common.result.Result;
import mb.common.util.ListView;
import mb.common.util.MapView;
import mb.common.util.MultiMapView;
import mb.constraint.common.ConstraintAnalyzer;
import mb.constraint.common.ConstraintAnalyzerContext;
import mb.constraint.common.ConstraintAnalyzerException;
import mb.constraint.pie.ConstraintAnalyzeMultiTaskDef;
import mb.pie.api.ExecContext;
import mb.pie.api.Function;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax2.common.primitive.generic.Spoofax2ProjectContext;
import mb.statix.StatixConfig;
import mb.statix.StatixConstraintAnalyzer;
import mb.statix.StatixScope;
import mb.stratego.common.StrategoRuntime;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import javax.inject.Provider;

@StatixScope
public class StatixAnalyzeMulti extends ConstraintAnalyzeMultiTaskDef {
    private final StatixConstraintAnalyzer constraintAnalyzer;
    private final Provider<StrategoRuntime> strategoRuntimeProvider;
    private final Function<ResourcePath, Result<Option<StatixConfig>, ?>> configFunction;

    @Inject
    public StatixAnalyzeMulti(
        StatixConstraintAnalyzer constraintAnalyzer,
        Provider<StrategoRuntime> strategoRuntimeProvider,
        Function<ResourcePath, Result<Option<StatixConfig>, ?>> configFunction
    ) {
        this.constraintAnalyzer = constraintAnalyzer;
        this.strategoRuntimeProvider = strategoRuntimeProvider;
        this.configFunction = configFunction;
    }

    @Override
    public String getId() {
        return "mb.statix.task.GeneratedStatixAnalyzeMulti";
    }

    @Override
    protected ConstraintAnalyzer.MultiFileResult analyze(ExecContext context, ResourcePath root, MapView<ResourceKey, IStrategoTerm> asts, ConstraintAnalyzerContext constraintAnalyzerContext) throws ConstraintAnalyzerException {
        return configFunction.apply(context, root).mapThrowingOrElse(
            o -> o.mapThrowingOrElse(
                c -> constraintAnalyzer.analyze(root, asts, constraintAnalyzerContext, strategoRuntimeProvider.get().addContextObject(createProjectContext(c))),
                ConstraintAnalyzer.MultiFileResult::new // Statix is not configured, do not need to analyze.
            ),
            e -> new ConstraintAnalyzer.MultiFileResult(KeyedMessages.of(ListView.of(new Message("Cannot check Statix files; reading configuration failed unexpectedly", e)), root))
        );
    }

    private Spoofax2ProjectContext createProjectContext(StatixConfig config) {
        final String languageId = "statix";
        return new Spoofax2ProjectContext(config.projectPath, MultiMapView.of(languageId, config.sourcePaths), MultiMapView.of(languageId, config.includePaths));
    }
}
