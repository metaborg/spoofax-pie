package mb.statix.task.spoofax;

import mb.common.message.KeyedMessages;
import mb.common.message.Message;
import mb.common.util.ListView;
import mb.common.util.MapView;
import mb.common.util.MultiMapView;
import mb.constraint.common.ConstraintAnalyzer;
import mb.constraint.common.ConstraintAnalyzerContext;
import mb.constraint.common.ConstraintAnalyzerException;
import mb.constraint.pie.ConstraintAnalyzeMultiTaskDef;
import mb.pie.api.ExecContext;
import mb.resource.ResourceKey;
import mb.resource.ResourceService;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax2.common.primitive.generic.Spoofax2ProjectContext;
import mb.statix.StatixConstraintAnalyzer;
import mb.statix.StatixScope;
import mb.statix.task.StatixConfig;
import mb.stratego.common.StrategoRuntime;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import javax.inject.Provider;

@StatixScope
public class StatixAnalyzeMultiWrapper extends ConstraintAnalyzeMultiTaskDef {
    private final StatixConstraintAnalyzer constraintAnalyzer;
    private final ResourceService resourceService;
    private final Provider<StrategoRuntime> strategoRuntimeProvider;
    private final StatixConfigFunctionWrapper configFunctionWrapper;

    @Inject
    public StatixAnalyzeMultiWrapper(
        StatixConstraintAnalyzer constraintAnalyzer,
        ResourceService resourceService,
        Provider<StrategoRuntime> strategoRuntimeProvider,
        StatixConfigFunctionWrapper configFunctionWrapper
    ) {
        this.constraintAnalyzer = constraintAnalyzer;
        this.resourceService = resourceService;
        this.strategoRuntimeProvider = strategoRuntimeProvider;
        this.configFunctionWrapper = configFunctionWrapper;
    }

    @Override
    public String getId() {
        return getClass().getName();
    }

    @Override
    protected ConstraintAnalyzer.MultiFileResult analyze(ExecContext context, ResourcePath root, MapView<ResourceKey, IStrategoTerm> asts, ConstraintAnalyzerContext constraintAnalyzerContext) throws ConstraintAnalyzerException {
        return configFunctionWrapper.get().apply(context, root).mapThrowingOrElse(
            o -> o.mapThrowingOrElse(
                c -> constraintAnalyzer.analyze(root, asts, constraintAnalyzerContext, strategoRuntimeProvider.get().addContextObject(createProjectContext(c)), resourceService),
                ConstraintAnalyzer.MultiFileResult::new // Statix is not configured, do not need to analyze. // TODO: redirect to base analysis?
            ),
            e -> new ConstraintAnalyzer.MultiFileResult(KeyedMessages.of(ListView.of(new Message("Cannot check Statix files; reading configuration failed unexpectedly", e)), root))
        );
    }

    private Spoofax2ProjectContext createProjectContext(StatixConfig config) {
        final String languageId = "statix";
        return new Spoofax2ProjectContext(config.rootDirectory, MultiMapView.of(languageId, config.sourcePaths), MultiMapView.of(languageId, config.includePaths));
    }
}
