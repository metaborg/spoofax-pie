package mb.statix.multilang;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Message;
import mb.common.util.UncheckedException;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.ITermVar;
import mb.nabl2.terms.build.ImmutableTermVar;
import mb.nabl2.terms.stratego.StrategoTerms;
import mb.nabl2.terms.unification.ud.IUniDisunifier;
import mb.nabl2.util.ImmutableTuple3;
import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.resource.ResourceKey;
import mb.statix.constraints.CConj;
import mb.statix.constraints.CExists;
import mb.statix.constraints.CNew;
import mb.statix.constraints.CUser;
import mb.statix.constraints.Constraints;
import mb.statix.constraints.messages.IMessage;
import mb.statix.constraints.messages.MessageUtil;
import mb.statix.multilang.utils.MessageUtils;
import mb.statix.multilang.utils.SpecUtils;
import mb.statix.solver.IConstraint;
import mb.statix.solver.IState;
import mb.statix.solver.completeness.IsComplete;
import mb.statix.solver.log.IDebugContext;
import mb.statix.solver.log.NullDebugContext;
import mb.statix.solver.persistent.Solver;
import mb.statix.solver.persistent.SolverResult;
import mb.statix.solver.persistent.State;
import mb.statix.spec.Spec;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.util.functions.Function2;
import org.metaborg.util.iterators.Iterables2;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Shallow solver maintaining context for 1 solver run
 */
class SolverContext {

    private final AnalysisContext analysisContext;
    private final ExecContext context;
    private final ITermFactory tf;
    private final StrategoTerms st;
    private final IDebugContext debug;
    private final Spec combinedSpec;
    private final Map<IConstraint, IMessage> messageMap = new HashMap<>();
    private final KeyedMessagesBuilder messages = new KeyedMessagesBuilder();

    public SolverContext(AnalysisContext analysisContext, ExecContext context, ITermFactory tf) {
        this.analysisContext = analysisContext;
        this.debug = new NullDebugContext(); // TODO: derive from analysisContext#getLogLevel & implement logging
        this.context = context;
        this.tf = tf;
        this.st = new StrategoTerms(tf);

        // Create initial state from combined specifications
        combinedSpec = analysisContext.languages().stream()
            .map(LanguageMetadata::statixSpec)
            .reduce(SpecUtils::mergeSpecs)
            .orElseThrow(() -> new RuntimeException("Doing analysis without specs is not allowed"));

        // TODO: Exception on overlapping constraints
    }

    public KeyedMessages execute() throws InterruptedException {
        // Get initial state. This state contains all project constraints which are solved partially
        final CachedAnalysis initial = getInitialState();
        final SolverResult initialResult = initial.globalState();
        final IState.Immutable initialState = initialResult.state();
        final ITerm globalScope = initial.globalScope();

        // Partial solve file results
        final List<SolverResult> results = solveFileConstraints(State.builder().from(initialState).build(), globalScope);

        // Update messages
        results.forEach(solverResult -> solverResult.messages()
            .forEach((constraint, message) -> processMessage(constraint, message, solverResult.state().unifier())));

        // Create composed state to use for final constraint solving
        IState.Immutable state = results.stream()
            .map(SolverResult::state)
            .reduce(initialState, IState.Immutable::add);

        // Collect delayed constraints
        final List<IConstraint> constraints = new ArrayList<>(initialResult.delays().keySet());
        results.stream()
            .map(SolverResult::delayed)
            .forEach(constraints::add);

        // Completely solve project constraints // TODO: timing
        final SolverResult globalResult = Solver.solve(combinedSpec, state, Constraints.conjoin(constraints), (s, l, st) -> true, debug);

        // Create messages
        final IUniDisunifier resultUnifier = globalResult.state().unifier();
        globalResult.messages().forEach((constraint, message) -> processMessage(constraint, message, resultUnifier));
        globalResult.delays().keySet().forEach(constraint -> processMessage(constraint,
            MessageUtil.findClosestMessage(constraint), resultUnifier));

        SolverResult finalResult = globalResult.withMessages(messageMap);

        return messages.build();
    }

    private List<SolverResult> solveFileConstraints(State initial, ITerm globalScope) {
        List<ImmutableTuple3<String, IConstraint, Function2<String, IConstraint, SolverResult>>> constraints = analysisContext
            .languages()
                .stream()
                .parallel()
                .flatMap(lang -> getFileConstraints(lang, initial, globalScope))
                .collect(Collectors.toList()); // Collect first to make timing similar to regular solver
        final double t0 = System.currentTimeMillis();
        final List<SolverResult> results = constraints.stream().parallel()
            .map(langConstraints -> langConstraints._3().apply(langConstraints._1(), langConstraints._2()))
            .collect(Collectors.toList());
        final double dt = System.currentTimeMillis() - t0;

        return results;
    }

    private Stream<ImmutableTuple3<String, IConstraint, Function2<String, IConstraint, SolverResult>>> getFileConstraints(
        LanguageMetadata lang, IState.Immutable initial, ITerm globalScope)
    {
        final Function2<String, IConstraint, SolverResult> solveConstraint =
            (resource, constraint) -> solveConstraint(lang.statixSpec(), initial.withResource(resource), constraint);
        String fileConstraintName = lang.fileConstraint();

        try {
            return lang.resourcesSupplier().get(context)
                .stream()
                .map(key -> {
                    try {
                        IStrategoTerm ast = lang.astFunction().apply(context, key);
                        Iterable<ITerm> constraintArgs = Iterables2.from(globalScope, st.fromStratego(ast));
                        IConstraint fileConstraint = new CUser(fileConstraintName, constraintArgs, null);
                        return ImmutableTuple3.of(key.toString(), fileConstraint, solveConstraint);
                    } catch(ExecException | InterruptedException e) {
                        throw new UncheckedException(e);
                    }
                });
        } catch(ExecException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private CachedAnalysis getInitialState() {
        // TODO: reuse cached results

        // Create constraint that calls all language's project constraints with a shared global scope
        ITermVar globalScopeVar = ImmutableTermVar.of("", "s");
        Iterable<ITermVar> scopeArgs = Iterables2.singleton(globalScopeVar);
        IConstraint globalConstraint = new CExists(scopeArgs, analysisContext.languages().stream()
            .map(LanguageMetadata::projectConstraint)
            .map(pc -> (IConstraint) new CUser(pc, scopeArgs))
            .reduce(new CNew(Iterables2.fromConcat(scopeArgs)), CConj::new));

        SolverResult partialProjectSolution = solveConstraint(combinedSpec, State.of(combinedSpec), globalConstraint);

        ITerm globalScope = partialProjectSolution.state().unifier()
            .findRecursive(partialProjectSolution.existentials().getOrDefault(globalScopeVar, globalScopeVar));

        return analysisContext.getCachedAnalysis()
            .withGlobalConstraint(globalConstraint)
            .withGlobalScope(globalScope)
            .withGlobalState(partialProjectSolution);
    }

    private SolverResult solveConstraint(Spec spec, IState.Immutable state, IConstraint constraint) {
        final IsComplete isComplete = (s, l, st) -> !state.scopes().contains(s);
        try {
            return Solver.solve(spec, state, constraint, isComplete, debug);
        } catch(InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void processMessage(IConstraint constraint, IMessage message, IUniDisunifier unifier) {
        messageMap.put(constraint, message);
        Message msg = MessageUtils.formatMessage(message, constraint, unifier);
        @Nullable ResourceKey resourceKey = message.origin()
            .map(MessageUtils::resourceKeyFromOrigin).orElse(null);
        messages.addMessages(resourceKey, Iterables2.singleton(msg));
    }
}
