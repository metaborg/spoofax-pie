package mb.dynamix.task.spoofax;

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
import mb.dynamix.DynamixConstraintAnalyzer;
import mb.dynamix.DynamixScope;
import mb.dynamix.task.DynamixConfig;
import mb.stratego.common.StrategoRuntime;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import javax.inject.Provider;

@DynamixScope
public class DynamixAnalyzeMultiWrapper extends ConstraintAnalyzeMultiTaskDef {
    private final DynamixConstraintAnalyzer constraintAnalyzer;
    private final ResourceService resourceService;
    private final Provider<StrategoRuntime> strategoRuntimeProvider;
    private final DynamixConfigFunctionWrapper configFunctionWrapper;

    @Inject
    public DynamixAnalyzeMultiWrapper(
        DynamixConstraintAnalyzer constraintAnalyzer,
        ResourceService resourceService,
        Provider<StrategoRuntime> strategoRuntimeProvider,
        DynamixConfigFunctionWrapper configFunctionWrapper
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
                ConstraintAnalyzer.MultiFileResult::new // Dynamix is not configured, do not need to analyze. // TODO: redirect to base analysis?
            ),
            e -> new ConstraintAnalyzer.MultiFileResult(KeyedMessages.of(ListView.of(new Message("Cannot check Dynamix files; reading configuration failed unexpectedly", e)), root))
        );
    }

    private Spoofax2ProjectContext createProjectContext(DynamixConfig config) {
        final String languageId = "dynamix";
        return new Spoofax2ProjectContext(config.rootDirectory, MultiMapView.of(languageId, config.sourcePaths), MultiMapView.of(languageId, config.includePaths));
    }
}
