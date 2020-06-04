package mb.statix.common.context;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Message;
import mb.common.message.Severity;
import mb.common.region.Region;
import mb.common.util.ListView;
import mb.common.util.UncheckedException;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.ITermVar;
import mb.nabl2.terms.build.ImmutableTermVar;
import mb.nabl2.terms.matching.TermMatch;
import mb.nabl2.terms.stratego.StrategoTerms;
import mb.nabl2.util.ImmutableTuple2;
import mb.nabl2.util.Tuple2;
import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.ResourceStringSupplier;
import mb.pie.api.TaskDef;
import mb.resource.DefaultResourceKey;
import mb.resource.ResourceKeyString;
import mb.resource.ResourceService;
import mb.statix.constraints.CExists;
import mb.statix.constraints.CNew;
import mb.statix.constraints.CUser;
import mb.statix.constraints.messages.IMessage;
import mb.statix.constraints.messages.MessageKind;
import mb.statix.scopegraph.terms.Scope;
import mb.statix.solver.IConstraint;
import mb.statix.solver.IState;
import mb.statix.solver.completeness.IsComplete;
import mb.statix.solver.log.IDebugContext;
import mb.statix.solver.log.NullDebugContext;
import mb.statix.solver.persistent.Solver;
import mb.statix.solver.persistent.SolverResult;
import mb.statix.solver.persistent.State;
import mb.statix.spec.Spec;
import mb.statix.spoofax.StatixTerms;
import mb.statix.utils.MessageUtils;
import mb.statix.utils.SpecUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.util.functions.Function1;
import org.metaborg.util.iterators.Iterables2;
import org.spoofax.interpreter.terms.ITermFactory;

import java.io.IOException;
import java.io.Serializable;
import java.security.Key;
import java.util.List;
import java.util.stream.Collectors;

public class StatixAnalysisTaskDef implements TaskDef<StatixAnalysisTaskDef.Input, @Nullable KeyedMessages> {

    public static class Input implements Serializable {
        private AnalysisContext context;

        public Input(AnalysisContext context) {
            this.context = context;
        }
    }

    private ITermFactory termFactory;

    public StatixAnalysisTaskDef(ITermFactory termFactory) {
        this.termFactory = termFactory;
    }

    @Override
    public String getId() {
        return StatixAnalysisTaskDef.class.getSimpleName();
    }

    @Override
    public @Nullable KeyedMessages exec(ExecContext context, Input input) throws Exception {
        // Todo: add loglevel to context & instantiate correct logger for LoggerDebugContext
        final IDebugContext debug = new NullDebugContext();
        final StrategoTerms strategoTerms = new StrategoTerms(termFactory);
        final @Nullable State initial = getInitialState(input.context, debug);

        List<ImmutableTuple2<ImmutableTuple2<String, CUser>,
            Function1<Tuple2<String, CUser>, SolverResult>>> constraints = input.context.languages()
            .stream()
            .parallel()
            .flatMap(lang -> {
                Spec spec = lang.statixSpec();
                final Function1<Tuple2<String, CUser>, SolverResult> solveConstraint =
                    resource_constraint -> solveConstraint(spec, initial.withResource(resource_constraint._1()),
                        resource_constraint._2(), debug);
                try {
                    return lang.resourcesSupplier().get(context)
                        .stream()
                        .map(key -> {
                            try {
                                return ImmutableTuple2.of(key.toString(),
                                    new CUser(lang.fileConstraint(),
                                        Iterables2.from(strategoTerms.fromStratego(lang.astSupplier()
                                            .apply(context, new ResourceStringSupplier(key))), Scope.of("", "s_1-1")),null));
                            } catch(ExecException | InterruptedException e) {
                                throw new UncheckedException(e);
                            }
                        })
                        .map(c -> ImmutableTuple2.of(c, solveConstraint));
                } catch(ExecException | IOException | InterruptedException e) {
                    throw new UncheckedException(e);
                }
            }).collect(Collectors.toList()); // Collect first to make timing similar to regular solver
        final double t0 = System.currentTimeMillis();
        final List<SolverResult> results = constraints.stream().parallel()
            .map(langConstraints -> langConstraints._2().apply(langConstraints._1()))
            .collect(Collectors.toList());
        final double dt = System.currentTimeMillis() - t0;
        input.context.updateSolverResult(State.builder().from(results.get(0).state()).build());

        // TODO: solve project constraints
        // TODO: mark delays as errors

        // Create messages
        KeyedMessagesBuilder builder = new KeyedMessagesBuilder();
        results.stream().forEach(res -> {
            List<Message> messages = res.messages().entrySet().stream()
                .map(entry -> MessageUtils.formatMessage(entry.getValue(), entry.getKey(), res.state().unifier()))
                .collect(Collectors.toList());
            ResourceKeyString key = ResourceKeyString.parse(res.state().resource());
            builder.addMessages(new DefaultResourceKey(key.getQualifier(), key.getId()), messages);
        });

        return builder.build();
    }

    private State getInitialState(AnalysisContext context, IDebugContext debug) {
        @Nullable State initial = context.getCachedResult();

        if(initial == null) {
            // Create initial state from combined specifications
            final Spec combinedSpec = context.languages().stream()
                .map(LanguageMetadata::statixSpec)
                .reduce(new SpecUtils(termFactory).initialSpec(), SpecUtils::mergeSpecs);
            initial = State.of(combinedSpec);
            // Create root scope instance
            IConstraint rootScopeConstraint = new CExists(Iterables2.singleton(ImmutableTermVar.of("", "s")),
                new CNew(Iterables2.singleton(ImmutableTermVar.of("", "s"))));
            initial = State.builder()
                .from(solveConstraint(combinedSpec, initial, rootScopeConstraint, debug).state())
                .build();
        }
        return initial;
    }

    private SolverResult solveConstraint(Spec spec, IState.Immutable state, IConstraint constraint, IDebugContext debug) {
        final IsComplete isComplete = (s, l, st) -> !state.scopes().contains(s);
        final SolverResult resultConfig;
        try {
            resultConfig = Solver.solve(spec, state, constraint, isComplete, debug);
        } catch(InterruptedException e) {
            throw new RuntimeException(e);
        }
        return resultConfig;
    }
}
