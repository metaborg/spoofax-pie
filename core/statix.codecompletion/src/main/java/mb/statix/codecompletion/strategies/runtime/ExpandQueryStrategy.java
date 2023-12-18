package mb.statix.codecompletion.strategies.runtime;

import io.usethesource.capsule.Map;
import io.usethesource.capsule.Set;
import mb.nabl2.terms.IListTerm;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.ITermVar;
import mb.nabl2.terms.ListTerms;
import mb.nabl2.terms.build.TermBuild;
import mb.nabl2.terms.unification.OccursException;
import mb.nabl2.terms.unification.RigidException;
import mb.nabl2.terms.unification.u.IUnifier;
import mb.nabl2.terms.unification.ud.IUniDisunifier;
import mb.scopegraph.oopsla20.reference.DataLeq;
import mb.scopegraph.oopsla20.reference.DataWF;
import mb.scopegraph.oopsla20.reference.EdgeOrData;
import mb.scopegraph.oopsla20.reference.Env;
import mb.scopegraph.oopsla20.reference.FastNameResolution;
import mb.scopegraph.oopsla20.reference.IncompleteException;
import mb.scopegraph.oopsla20.reference.LabelOrder;
import mb.scopegraph.oopsla20.reference.LabelWF;
import mb.scopegraph.oopsla20.reference.RegExpLabelWF;
import mb.scopegraph.oopsla20.reference.RelationLabelOrder;
import mb.scopegraph.oopsla20.reference.ResolutionException;
import mb.scopegraph.oopsla20.reference.ResolutionInterpreter;
import mb.scopegraph.oopsla20.terms.newPath.ResolutionPath;
import mb.scopegraph.resolution.StateMachine;
import mb.statix.codecompletion.CCSolverState;
import mb.statix.codecompletion.SelectedConstraintCCSolverState;
import mb.statix.codecompletion.SolverContext;
import mb.statix.constraints.CAstId;
import mb.statix.constraints.CCompiledQuery;
import mb.statix.constraints.CEqual;
import mb.statix.constraints.CInequal;
import mb.statix.constraints.CResolveQuery;
import mb.statix.constraints.IResolveQuery;
import mb.statix.generator.scopegraph.Match;
import mb.statix.generator.scopegraph.NameResolution;
import mb.statix.scopegraph.Scope;
import mb.statix.solver.Delay;
import mb.statix.solver.IConstraint;
import mb.statix.solver.IState;
import mb.statix.solver.completeness.ICompleteness;
import mb.statix.solver.completeness.IsComplete;
import mb.statix.solver.log.NullDebugContext;
import mb.statix.solver.persistent.Solver;
import mb.statix.solver.persistent.SolverResult;
import mb.statix.solver.persistent.query.ConstraintQueries;
import mb.statix.spec.ApplyMode;
import mb.statix.spec.ApplyResult;
import mb.statix.spec.PreSolvedConstraint;
import mb.statix.spec.Rule;
import mb.statix.spec.RuleUtil;
import mb.statix.spec.Spec;
import mb.statix.spoofax.StatixTerms;
import mb.tego.sequences.Seq;
import mb.tego.strategies.NamedStrategy2;
import mb.tego.strategies.runtime.TegoEngine;
import mb.tego.utils.StreamUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.util.collection.CapsuleUtil;
import org.metaborg.util.collection.ImList;
import org.metaborg.util.functions.Predicate2;
import org.metaborg.util.iterators.Iterables2;
import org.metaborg.util.optionals.Optionals;
import org.metaborg.util.task.NullCancel;
import org.metaborg.util.task.NullProgress;
import org.metaborg.util.tuple.Tuple2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static mb.nabl2.terms.build.TermBuild.B;
import static mb.nabl2.terms.matching.TermMatch.M;

/**
 * Expands the selected query.
 */
@SuppressWarnings("UnstableApiUsage")
public final class ExpandQueryStrategy extends NamedStrategy2<SolverContext, ITermVar, SelectedConstraintCCSolverState<IResolveQuery>, Seq<CCSolverState>> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final ExpandQueryStrategy instance = new ExpandQueryStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static ExpandQueryStrategy getInstance() { return (ExpandQueryStrategy)instance; }

    private ExpandQueryStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    @Override
    public String getName() {
        return "expandQuery";
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    @Override
    public String getParamName(int index) {
        switch (index) {
            case 0: return "ctx";
            case 1: return "v";
            default: return super.getParamName(index);
        }
    }

    @Override
    public Seq<CCSolverState> evalInternal(
        TegoEngine engine,
        SolverContext ctx,
        ITermVar v,
        SelectedConstraintCCSolverState<IResolveQuery> input
    ) {
        return eval(engine, ctx, v, input);
    }

    /**
     * Evaluates the strategy.
     *
     * @param engine the Tego engine
     * @param ctx the context
     * @param v the placeholder variable
     * @param input the input state
     * @return the resulting sequence of states
     */
    public static Seq<CCSolverState> eval(
        TegoEngine engine,
        SolverContext ctx,
        ITermVar v,
        SelectedConstraintCCSolverState<IResolveQuery> input
    ) {
        // Get the query to expand
        final IResolveQuery query = input.getSelected();
        final IState.Immutable state = input.getState();
        final IUniDisunifier.Immutable unifier = state.unifier();
        engine.log(instance, "Expand query: {}", query);

        // Find the scope. If it is not ground, delay the query.
        if(!unifier.isGround(query.scopeTerm())) {
            // Delay
            final Delay delay = Delay.ofVars(unifier.getVars(query.scopeTerm()));
            return Seq.of(input.withoutSelected().withDelay(query, delay));
        }
        @Nullable final Scope scope = Scope.matcher().match(query.scopeTerm(), unifier).orElse(null);
        assert scope != null;

        // Determine data equivalence (either: true, false, or null when it could not be determined)
        @Nullable final Boolean isAlways;
        try {
            isAlways = query.min().getDataEquiv().isAlways().orElse(null);
        } catch(InterruptedException e) {
            // TODO: Should strategies throw InterruptedException
            throw new RuntimeException(e);
        }
        if(isAlways == null) {
            throw new IllegalArgumentException("cannot resolve query: cannot decide data equivalence");
        }

        //final List<SolverState> output = expandQuerySlow(query, unifier, state, isAlways, scope, engine, input);
        final List<CCSolverState> output = expandQueryFast(query, unifier, state, isAlways, scope, engine, input);

        @Nullable final ITermVar focusVar = ctx.getFocusVar();
        if (focusVar != null && engine.isLogEnabled(instance)) {
            for(CCSolverState s : output) {
                engine.log(instance, "- {}", s.project(focusVar));
            }
        }

        return Seq.from(output);
    }

    private static int cached = 0;
    private static int uncached = 0;

    // Old algorithm
    /*
    private static List<CCSolverState> expandQuerySlow(
        CResolveQuery query,
        IUniDisunifier.Immutable unifier,
        IState.Immutable state,
        @Nullable Boolean isAlways,
        Scope scope,
        TegoEngine engine,
        SelectedConstraintCCSolverState<CResolveQuery> input
    ) {

        final ICompleteness.Immutable completeness = input.getCompleteness();
        final LabelWF<ITerm> labelWF = RegExpLabelWF.of(query.filter().getLabelWF());
        final LabelOrder<ITerm> labelOrd = new RelationLabelOrder<>(query.min().getLabelOrder());
        final DataWF<ITerm, CEqual> dataWF = new ResolveDataWF(state, completeness, query.filter().getDataWF(), query);
        final Predicate2<Scope, EdgeOrData<ITerm>> isComplete =
            (s, l) -> completeness.isComplete(s, l, state.unifier());

        // @formatter:off
        final NameResolution<Scope, ITerm, ITerm, CEqual> nameResolution = new NameResolution<>(
            input.getSpec(),
            state.scopeGraph(),
            input.getSpec().allLabels(),
            labelWF, labelOrd,
            dataWF, isAlways, isComplete);
        // @formatter:on

        engine.log(instance, "Expanding:");

        // Find all possible declarations the resolution could resolve to.
        final int declarationCount;
        try {
            declarationCount = countDeclarations(nameResolution, scope);
            engine.log(instance, "  ▶ found {} declarations", declarationCount);
        } catch(IncompleteException e) {
            // Delay
            final Delay delay = Delay.ofVars(unifier.getVars(query.scopeTerm()));
            return Collections.singletonList(input.withoutSelected().withDelay(query, delay));
        }

        // For each declaration:
        final ArrayList<CCSolverState> output = new ArrayList<>();
        for (int i = 0; i < declarationCount; i++) {
            final List<CCSolverState> newStates = expandResolution(engine, input.getSpec(), query, input.withoutSelected(),
                unifier, nameResolution, scope, i);
            engine.log(instance, "  ▶ added {} possible states", newStates.size());
            output.addAll(newStates);
        }
        engine.log(instance, "▶ expanded {} declarations into {} possible states", declarationCount, output.size());
        return output;
    }
     */

    // New algorithm
    private static List<CCSolverState> expandQueryFast(
        IResolveQuery query,
        IUniDisunifier.Immutable unifier,
        IState.Immutable state,
        @Nullable Boolean isAlways,
        Scope scope,
        TegoEngine engine,
        SelectedConstraintCCSolverState<IResolveQuery> input
    ) {

        final ICompleteness.Immutable completeness = input.getCompleteness();
        final LabelWF<ITerm> labelWF = RegExpLabelWF.of(query.filter().getLabelWF());
        final LabelOrder<ITerm> labelOrd = new RelationLabelOrder<>(query.min().getLabelOrder());
        final Predicate2<Scope, EdgeOrData<ITerm>> isComplete2 =
            (s, l) -> completeness.isComplete(s, l, state.unifier());
        final IsComplete isComplete3 =
            (s, l, st) -> isComplete2.test(s, l);
        final ConstraintQueries constraintQueries = new ConstraintQueries(input.getSpec(), state, isComplete3);

        final HashMap<ITerm, Optional<SolverResult<?>>> cache = new HashMap<>();

        final DataWF<ITerm> dataWF = t ->
            // Assert that we can apply the dataWF to the input
            applyDataWFCached(cache, query.filter().getDataWF(), t, state, unifier, completeness, input.getSpec()).isPresent();
        final DataLeq<ITerm> dataEq = new DataLeq<ITerm>() {
            @Override public boolean leq(ITerm d1, ITerm d2)throws ResolutionException, InterruptedException {
                // Apply the dataWF to each of the inputs. This should result in two solver results
                // If this is not the case, this would already have failed the lambda in withDataWF().
                final SolverResult<?> result1 = applyDataWFCached(cache, query.filter().getDataWF(), d1, state, unifier,  completeness, input.getSpec() ).get();
                final SolverResult<?> result2 = applyDataWFCached(cache, query.filter().getDataWF(), d2, state, unifier,  completeness, input.getSpec() ).get();

                // For each free variable in the query body...
                final Set.Immutable<ITermVar> varsToCompare = query.filter().getDataWF().body().freeVars();
                boolean maybeShadowing = true;
                for (ITermVar v : varsToCompare) {
                    // Compare the values given to them in the respective dataWF applications
                    final ITerm t1 = result1.state().unifier().findRecursive(v);
                    final ITerm t2 = result2.state().unifier().findRecursive(v);
                    try {
                        // Attempt to unify the two terms, given the current unifier
                        final Optional<IUniDisunifier.Result<IUnifier.Immutable>> unify = unifier.unify(t1, t2, rv -> state.vars().contains(rv));
                        if (unify.isPresent()) {
                            // The two unifiers unify, meaning that they have the same values for their variables
                            // This means that they could normally have shadowed each other.
                            // So we might apply shadowing.
                            maybeShadowing = true;
                        } else {
                            // The two unifiers don't unify, meaning that they have different values for their variables
                            // This means that they would normally not have shadowed each other.
                            // So we apply definitely no shadowing.
                            maybeShadowing = false;
                            break;
                        }
                    } catch (OccursException e) {
                        throw new RuntimeException(e);
                    } catch (RigidException e) {
                        // We might apply shadowing?
                        // When the variable is rigid, we could not unify.
                        // Therefore, we don't know whether we are shadowing or not.
                        maybeShadowing = true;
                        // FIXME: is this correct?
                        engine.log(instance, "Rigid!! " + e.getMessage());
                        engine.log(instance, "When expanding: " + query);
                    }
                }

                if (maybeShadowing) {
                    // We apply shadowing by checking the dataEquiv:
                    final DataLeq<ITerm> leq = constraintQueries.getDataEquiv(query.min().getDataEquiv());
                    return leq.leq(d1, d2);
                } else {
                    // No shadowing.
                    return false;
                }
            }

            @Override public boolean alwaysTrue() throws InterruptedException {
                // Definitely not always true.
                return false;
            }
        };

        final F1<Scope, Env<Scope, ITerm, ITerm>> resolveFn = query.match(new IResolveQuery.Cases<F1<Scope, Env<Scope, ITerm, ITerm>>>()  {
            @Override
            public F1<Scope, Env<Scope, ITerm, ITerm>> caseResolveQuery(CResolveQuery q) {
                final FastNameResolution<Scope, ITerm, ITerm> nameResolution = FastNameResolution.<Scope, ITerm, ITerm>builder()
                    .withLabelWF(labelWF)
                    .withLabelOrder(labelOrd)
                    .withDataWF(dataWF)
                    .withDataEquiv(dataEq)
                    .withIsComplete(isComplete2)
                    .build(state.scopeGraph(),input.getSpec().allLabels() );
                return s -> nameResolution.resolve(scope, new NullCancel());
            }

            @Override
            public F1<Scope, Env<Scope, ITerm, ITerm>> caseCompiledQuery(CCompiledQuery q) {
                final StateMachine<ITerm> stateMachine = q.stateMachine();

                final ResolutionInterpreter<Scope, ITerm, ITerm> resolutionInterpreter = new ResolutionInterpreter<>(
                    state.scopeGraph(),
                    dataWF,
                    dataEq,
                    stateMachine,
                    isComplete2);
                return s -> resolutionInterpreter.resolve(scope, new NullCancel());
            }
        });

        // Now, if we apply this name resolution,
        // we will end up with an environment with _all_ possible resolution paths.
        final Env<Scope, ITerm, ITerm> env;
        try {
            env = resolveFn.apply(scope);
        } catch(IncompleteException e) {
            // Delay
            final Delay delay = Delay.ofVars(unifier.getVars(query.scopeTerm()));
            return Collections.singletonList(input.withoutSelected().withDelay(query, delay));
        } catch(ResolutionException e) {
            throw new RuntimeException("Unexpected ResolutionException: " + e.getMessage(), e);
        } catch(InterruptedException e) {
            throw new RuntimeException("Unexpected InterruptedException: " + e.getMessage(), e);
        }

        // TODO: Group by:
        // env contains all possible resolution paths
        // To get the paths that would be in the same query together,
        // we should group-by when the unifiers are equal modulo alpha-renaming
        // of existentials in the body of the data wellformedness.

        engine.log(instance, "Expanding:");

        //nameResolution.

//        // Find all possible declarations the resolution could resolve to.
//        final int declarationCount = countDeclarations(nameResolution, scope);
        final int declarationCount = env.size();
        engine.log(instance, "  ▶ found {} declarations", declarationCount);

        // For each declaration:
        final ArrayList<CCSolverState> output = new ArrayList<>();
        for(ResolutionPath<Scope, ITerm, ITerm> path : env) {
            final CCSolverState newState = updateSolverState(
                Collections.singletonList(path),
                input.getSpec(),
                query,
                input.withoutSelected(),
                unifier,
                completeness,
                cache
            );
            output.add(newState);
        }
        engine.log(instance, "▶ expanded {} declarations into {} possible states", declarationCount, output.size());
        engine.log(instance, "Applied dataWF {} times + cached {} times", uncached, cached);
        uncached = 0;
        cached = 0;
        return output;
    }

    private static Optional<SolverResult<?>> applyDataWFCached(HashMap<ITerm, Optional<SolverResult<?>>> cache, Rule dataWF, ITerm d, IState.Immutable state, IUniDisunifier.Immutable unifier, ICompleteness.Immutable completeness, Spec spec) {
        return cache.compute(d, (k, term) -> {
            if (term == null) {
                // Assert that we can apply the dataWF to the input
                uncached += 1;
                return applyDataWF(dataWF, d, state, unifier, completeness, spec);
            } else {
                cached += 1;
                return term;
            }
        });
    }


    /**
     * Applies the dataWF to the specified datum.
     *
     * applyDataWF :: DataWF * Data -> Unifier?
     *
     * @param dataWF       the data WF predicate
     * @param d            the datum
     * @param state        the current state
     * @param unifier      the unifier of the context
     * @param completeness the completeness of the context
     * @param spec         the Statix specification
     * @return either a {@link SolverResult} with the result of applying the {@code dataWF} to the given datum;
     * otherwise, nothing if the {@code dataWF} could not be applied
     */
    private static Optional<SolverResult<?>> applyDataWF(Rule dataWF, ITerm d, IState.Immutable state, IUniDisunifier.Immutable unifier, ICompleteness.Immutable completeness, Spec spec) {
        // Apply the 'dataWF' to the specified 'd'
        final ApplyResult applyResult;
        if ((applyResult = RuleUtil.apply(
            unifier,
            dataWF,
            Collections.singletonList(d),
            null,
            ApplyMode.RELAXED,
            ApplyMode.Safety.SAFE /* ? FIXME: not sure, ResolveDataWF uses UNSAFE */
        ).orElse(null)) == null) {
            return Optional.empty();
        }
        final IState.Immutable applyState = state;
        final IConstraint applyConstraint = applyResult.body();

        // Update completeness for new state and constraint
        final ICompleteness.Transient _completeness = completeness.melt();
        _completeness.add(applyConstraint, spec, applyState.unifier());

        final IConstraint constraint = applyResult.body();

        // TODO: Reutrn a PreSolveResult or find another way to merge the SolverResult and PreSolveResult
//        final Optional<Solver.PreSolveResult> solverResult = trySolveFast(constraint, state, _completeness, spec);
//        if (!solverResult.isPresent()) return Optional.empty();
//        final Solver.PreSolveResult result = solverResult.get();
//        final IState.Immutable newState = result.state;

        final Optional<SolverResult> solverResult = trySolveSlow(constraint, state, _completeness, spec);
        if (!solverResult.isPresent()) return Optional.empty();
        final SolverResult result = solverResult.get();
        final IState.Immutable newState = result.state();

        // NOTE: This part is almost a duplicate of ResolveDataWF::wf and should be kept in sync
        // NOTE: The retain operation is important because it may change
        //  representatives, which can be local to newUnifier.
        final IUniDisunifier.Immutable newUnifier = newState.unifier().retainAll(state.vars()).unifier();

        // Check that all (remaining) disequalities are implied (i.e., not unifiable) in the original unifier
        // @formatter:off
        final List<ITermVar> disunifiedVars = newUnifier.disequalities().stream()
            .filter(diseq -> diseq.toTuple().apply(unifier::disunify).map(r -> r.result().isPresent()).orElse(true))
            .flatMap(diseq -> diseq.domainSet().stream())
            .collect(Collectors.toList());
        // @formatter:on
        if(!disunifiedVars.isEmpty()) return Optional.empty();

        // Applying dataWF succeeded.
        return Optional.of(result);
    }


    private static Optional<Solver.PreSolveResult> trySolveFast(
        IConstraint constraint,
        IState.Immutable state,
        ICompleteness.Transient completeness,
        Spec spec
    ) {
        // NOTE: This part is almost a duplicate of Solver::preEntail and should be kept in sync
        final IState.Transient _state = state.melt();
        final IUniDisunifier.Transient _unifier = state.unifier().melt();
        final java.util.Set<ITermVar> _updatedVars = new HashSet<>();
        final List<IConstraint> constraints = new ArrayList<>();
        final java.util.Map<ITermVar, ITermVar> _existentials = new HashMap<>();
        final List<IConstraint> failures = new ArrayList<>();
        final java.util.Map<IConstraint, Delay> delays = new HashMap<>();
        PreSolvedConstraint.preSolve(
            constraint,
            _state::freshVars,
            _unifier,
            v -> !_state.vars().contains(v),
            _updatedVars,
            constraints,
            completeness,
            _existentials,
            failures,
            delays,
            constraint.cause().orElse(null),
            true
        );
        // If the solver state contains errors, applying dataWF failed.
        if (!failures.isEmpty()) return Optional.empty();

        // If the solver state contains delays, applying dataWF failed?
        // TODO: Delayed variables (those that are not in the unifier but may have constraints applied to them)
        //  indicate that we cannot really know if we can apply this. What do we do?
        // FIXME: For now, we accept this.
        //if(!delays.isEmpty()) return Optional.empty();

        final IState.Immutable newState = _state.freeze().withUnifier(_unifier.freeze());
        final Solver.PreSolveResult preSolveResult = new Solver.PreSolveResult(newState, _updatedVars, constraints,
            completeness.freeze(), _existentials, Collections.emptyMap());
        return Optional.of(preSolveResult);
    }

    private static Optional<SolverResult> trySolveSlow(
        IConstraint constraint,
        IState.Immutable state,
        ICompleteness.Transient completeness,
        Spec spec
    ) {
        // NOTE: This part is almost a duplicate of Solver::entails and should be kept in sync
        final SolverResult result;
        try {
            result = Solver.solve(
                spec,
                state,
                Iterables2.singleton(constraint),
                Map.Immutable.of(),
                completeness.freeze(),
                IsComplete.ALWAYS,
                new NullDebugContext(),
                new NullProgress(),
                new NullCancel(),
                Solver.RETURN_ON_FIRST_ERROR
            );
        } catch(InterruptedException e) {
            // Interrupted
            return Optional.empty();
        }
        // If the solver state contains errors, applying dataWF failed.
        if (result.hasErrors()) return Optional.empty();

        // If the solver state contains delays, applying dataWF failed?
        // TODO: Delayed variables (those that are not in the unifier but may have constraints applied to them)
        //  indicate that we cannot really know if we can apply this. What do we do?
        // FIXME: For now, we accept this.
        //if(!result.delays().isEmpty()) return Optional.empty();

        return Optional.of(result);
    }


    /**
     * Performs name resolution.
     *
     * @param spec the spec
     * @param query the query
     * @param inputState the input state
     * @param unifier the unifier
     * @param nameResolution the name resolution
     * @param scope the scope
     * @param index the zero-based index of the resolution
     * @return the list of new solver states
     */
    private static List<CCSolverState> expandResolution(
        TegoEngine engine,
        Spec spec,
        IResolveQuery query,
        CCSolverState inputState,
        IUniDisunifier unifier,
        NameResolution<Scope, ITerm, ITerm, CEqual> nameResolution,
        Scope scope,
        int index
    ) {
        final mb.statix.generator.scopegraph.Env<Scope, ITerm, ITerm, CEqual> env = resolveByIndex(nameResolution, scope, index);

        engine.log(instance, "  ▶ ▶ declaration #{}: {} matches", index, env.matches.size());

        // No matches, so no results
        if(env.matches.isEmpty()) {
            return ImList.Immutable.of();
        }

        // Conditional matches
        final List<Match<Scope, ITerm, ITerm, CEqual>> optMatches =
            env.matches.stream().filter(m -> m.condition.isPresent()).collect(Collectors.toList());
        // Unconditional matches
        final List<Match<Scope, ITerm, ITerm, CEqual>> reqMatches =
            env.matches.stream().filter(m -> !m.condition.isPresent()).collect(Collectors.toList());
        // Unconditional rejects
        final List<Match<Scope, ITerm, ITerm, CEqual>> reqRejects = env.rejects;

        engine.log(instance, "  ▶ ▶ conditional matches {}", optMatches);
        engine.log(instance, "  ▶ ▶ unconditional matches {}", reqMatches);
        engine.log(instance, "  ▶ ▶ unconditional rejects {}", reqRejects);

        // Group the conditional matches into groups that can be applied together
        final List<List<Match<Scope, ITerm, ITerm, CEqual>>> optMatchGroups = groupByCondition(optMatches);

        final List<CCSolverState> newStates = new ArrayList<>();
        for (List<Match<Scope, ITerm, ITerm, CEqual>> optMatchGroup : optMatchGroups) {
            engine.log(instance, "  ▶ ▶ ▶ match group {}", optMatchGroup);
            // Determine the range of sizes the query result set can be
            final Tuple2<Integer, Integer> sizes = resultSize(query.resultTerm(), unifier, optMatchGroup.size());

            engine.log(instance, "  ▶ ▶ ▶ sizes {}", sizes);

            // For each possible size:
            final List<CCSolverState> states = expandResolutionSets(
                engine,
                spec, query, inputState, sizes,
                optMatchGroup, reqMatches, reqRejects
            );
            newStates.addAll(states);
        }
        return newStates;
    }

    /**
     * Groups the matches such that their conditions can be true simultaneously.
     *
     * @param matches the conditional matches to group
     *
     * @return the groups of conditional matches
     */
    private static List<List<Match<Scope, ITerm, ITerm, CEqual>>> groupByCondition(List<Match<Scope, ITerm, ITerm, CEqual>> matches) {
        final Deque<Match<Scope, ITerm, ITerm, CEqual>> worklist = new LinkedList<>(matches);
        final List<List<Match<Scope, ITerm, ITerm, CEqual>>> matchGroups = new ArrayList<>();
        while (!worklist.isEmpty()) {
            final List<Match<Scope, ITerm, ITerm, CEqual>> matchGroup = new ArrayList<>();
            final Match<Scope, ITerm, ITerm, CEqual> match = worklist.remove();
            matchGroup.add(match);
            // This method only works on conditional matches anyway
            @SuppressWarnings("OptionalGetWithoutIsPresent") final CEqual condition = match.condition.get();

            final Iterator<Match<Scope, ITerm, ITerm, CEqual>> iterator = worklist.iterator();
            while (iterator.hasNext()) {
                final Match<Scope, ITerm, ITerm, CEqual> otherMatch = iterator.next();
                // This method only works on conditional matches anyway
                @SuppressWarnings("OptionalGetWithoutIsPresent") final CEqual otherCondition = otherMatch.condition.get();
                // FIXME: Can I use Unifier.diff() here? Performance?
                if (condition.term1().equals(otherCondition.term1())) {
                    if (condition.term2().equals(otherCondition.term2())) {
                        // (?v == "x") == (?v == "x")
                        matchGroup.add(otherMatch);
                        iterator.remove();
                    }
                } else {
                    // The two conditions are unrelated, so we will add them anyway.
                    // (?x == _), (?y == _)
                    matchGroup.add(otherMatch);
                    // But we don't remove it, so it gets added to other groups as well.
                }
            }
            matchGroups.add(matchGroup);
        }
        return matchGroups;
    }

    /**
     * Counts the number of declarations this name resolution could resolve to.
     *
     * @param nameResolution the name resolution
     * @param scope the starting scope
     * @return the number of declarations
     */
    private static int countDeclarations(
        NameResolution<Scope, ITerm, ITerm, CEqual> nameResolution,
        Scope scope
    ) throws IncompleteException {
        // Find all possible declarations the resolution could resolve to.
        final AtomicInteger count = new AtomicInteger(1);
        try {
            nameResolution.resolve(scope, () -> {
                count.incrementAndGet();
                return false;
            });
        }  catch(IncompleteException e) {
            throw e;
        } catch(ResolutionException e) {
            throw new IllegalArgumentException("cannot resolve query: delayed on " + e.getMessage());
        } catch(InterruptedException e) {
            throw new RuntimeException(e);
        }
        return count.get();
    }

    private static mb.statix.generator.scopegraph.Env<Scope, ITerm, ITerm, CEqual> resolveByIndex(
        NameResolution<Scope, ITerm, ITerm, CEqual> nameResolution,
        Scope scope,
        int index
    ) {
        final AtomicInteger select = new AtomicInteger(index);
        try {
            return nameResolution.resolve(scope, () -> select.getAndDecrement() == 0);
        } catch(ResolutionException e) {
            throw new IllegalArgumentException("cannot resolve query: delayed on " + e.getMessage());
        } catch(InterruptedException e) {
            // Unfortunate that we have to do this
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a new solver state for each subset of the given set of optional matches.
     *
     * @param spec       the spec
     * @param query      the query
     * @param state      the input state
     * @param sizes      the range of sizes
     * @param optMatches the optional matches
     * @param reqMatches the required matches
     * @param reqRejects the required rejects
     * @return a list of new solver states
     */
    private static List<CCSolverState> expandResolutionSets(
        TegoEngine engine,
        Spec spec,
        IResolveQuery query,
        CCSolverState state,
        Tuple2<Integer, Integer> sizes,
        Collection<Match<Scope, ITerm, ITerm, CEqual>> optMatches,
        Collection<Match<Scope, ITerm, ITerm, CEqual>> reqMatches,
        Collection<Match<Scope, ITerm, ITerm, CEqual>> reqRejects
    ) {
        // FIXME: Query should not return shadowed declarations, such as those that are further away
        // FIXME: Support queries that return multiple results
        return IntStream.rangeClosed(sizes._1(), sizes._2()).mapToObj(size ->
        {
            final Stream<Collection<Match<Scope, ITerm, ITerm, CEqual>>> subsets = StreamUtils.subsetsOfSize(optMatches.stream(), size);
            final List<Collection<Match<Scope, ITerm, ITerm, CEqual>>> subsetsList = subsets.collect(Collectors.toList());
            engine.log(instance, "  ▶ ▶ ▶ for size {}: {}", size, subsetsList);
            return subsetsList.stream().map(matches ->
                updateSolverStateOld(
                    spec, query, state,
                    // The selected matches are accepted
                    matches,
                    // The remaining matches are rejected
                    optMatches.stream().filter(m -> !matches.contains(m)).collect(Collectors.toList()),
                    // The required matches are accepted
                    reqMatches,
                    // The required rejects are rejected
                    reqRejects
                )
            );
        }).flatMap(stream -> stream).collect(Collectors.toList());
    }

    /**
     * Creates a new solver state for the given resolution path.
     *
     * @param paths        the resolution paths
     * @param spec         the spec
     * @param query        the query
     * @param state        the input state
     * @param unifier
     * @param completeness
     * @param cache
     * @return the new solver state
     */
    private static CCSolverState updateSolverState(
        Iterable<ResolutionPath<Scope, ITerm, ITerm>> paths,
        Spec spec,
        IResolveQuery query,
        CCSolverState state,
        IUniDisunifier.Immutable unifier, ICompleteness.Immutable completeness,
        HashMap<ITerm, Optional<SolverResult<?>>> cache
    ) {
        final ArrayList<ITerm> pathTerms = new ArrayList<>();
        final ArrayList<CEqual> eqs = new ArrayList<>();
        for (ResolutionPath<Scope, ITerm, ITerm> path : paths) {
            pathTerms.add(StatixTerms.pathToTerm(path, spec.dataLabels()));
            final SolverResult<?> solverResult = applyDataWFCached(cache, query.filter().getDataWF(), path.getDatum(), state.getState(), unifier, completeness, spec).get();
            for (ITermVar v : solverResult.updatedVars()) {
                eqs.add(new CEqual(v, solverResult.state().unifier().findRecursive(v)));
            }
        }

        final List<IConstraint> addConstraints = new ArrayList<>();
        addConstraints.add(new CEqual(TermBuild.B.newList(pathTerms), query.resultTerm(), query));
        addConstraints.addAll(eqs);
        final Iterable<IConstraint> remConstraints = Iterables2.singleton(query);

        // Update the given state with the added and removed constraints
        return state.withUpdatedConstraints(addConstraints, remConstraints)
            .withMeta(state.getMeta().withExpandedQueriesIncremented());
    }


    /**
     * Creates a new solver state for the given matches.
     *
     * @param spec       the spec
     * @param query      the query
     * @param state      the input state
     * @param optMatches the accepted optional matches
     * @param optRejects the rejected optional matches
     * @param reqMatches the required matches
     * @param reqRejects the required rejects
     * @return the new solver state
     */
    private static CCSolverState updateSolverStateOld(
        Spec spec,
        IResolveQuery query,
        CCSolverState state,
        Collection<Match<Scope, ITerm, ITerm, CEqual>> optMatches,
        Collection<Match<Scope, ITerm, ITerm, CEqual>> optRejects,
        Collection<Match<Scope, ITerm, ITerm, CEqual>> reqMatches,
        Collection<Match<Scope, ITerm, ITerm, CEqual>> reqRejects
    ) {
        // Build a new environment with all the matches and rejects.
        final mb.statix.generator.scopegraph.Env.Builder<Scope, ITerm, ITerm, CEqual> subEnvBuilder = mb.statix.generator.scopegraph.Env.builder();
        optMatches.forEach(subEnvBuilder::match);
        reqMatches.forEach(subEnvBuilder::match);
        optRejects.forEach(subEnvBuilder::reject);
        reqRejects.forEach(subEnvBuilder::reject);
        final mb.statix.generator.scopegraph.Env<Scope, ITerm, ITerm, CEqual> subEnv = subEnvBuilder.build();

        // Build the list of constraints to add
        final ImList.Mutable<IConstraint> addConstraints = ImList.Mutable.of();

        // The explicated match path must match the query result term
        final List<ITerm> pathTerms = subEnv.matches.stream().map(m -> StatixTerms.pathToTerm(m.path, spec.dataLabels()))
            .collect(ImList.toImmutableList());
        addConstraints.add(new CEqual(B.newList(pathTerms), query.resultTerm(), query));
        subEnv.matches.stream().flatMap(m -> Optionals.stream(m.condition)).forEach(addConstraints::add);
        subEnv.rejects.stream().flatMap(m -> Optionals.stream(m.condition)).forEach(condition -> addConstraints.add(
                new CInequal(CapsuleUtil.immutableSet(), condition.term1(), condition.term2(),
                    condition.cause().orElse(null), condition.message().orElse(null))
            )
        );

        // Build the list of constraints to remove
        final Iterable<IConstraint> remConstraints = Iterables2.singleton(query);

        // Update the given state with the added and removed constraints
        return state.withUpdatedConstraints(addConstraints.freeze(), remConstraints)
            .withMeta(state.getMeta().withExpandedQueriesIncremented());
    }

    /**
     * Removes any constraints that deal with Term IDs.
     *
     * Term IDs are used to distinguish occurrences and attach properties on specific nodes.
     * The invariant is that each term in the AST has a unique term ID. However,
     * terms that originate from the specification, such as pre-defined names, do not have an
     * associated term ID. Providing one for the literal would be incorrect, as any places where
     * its usage is inferred would share the same term ID.
     *
     * Since term properties are write-only, we remove any term ID constraints.
     *
     * NOTE: this is not correct when something like `Var{x@x}` is used in the spec!
     *
     * @param constraints the builder for the list of constraints
     */
    private static ImList.Immutable<IConstraint> removeTermIdConstraints(ImList.Immutable<IConstraint> constraints) {
        return constraints.stream().filter(c -> !(c instanceof CAstId)).collect(ImList.toImmutableList());
    }

    /**
     * Determine the possible sizes of the result sets of the query.
     *
     * Many queries expect to resolve to a set with a single declaration,
     * but this is not necessarily so. This method returns the possible
     * sizes of the query result set as a range. The minimum is 0 and the
     * maximum is the number of declarations found.
     *
     * We distinguish these cases:<ul>
     * <li>empty list [] -> [0]
     * <li>fixed-size list [a, b, c] -> [3]
     * <li>variable-length list [a, b | xs] -> [2, declarationCount]
     * <li>something else -> [0, declarationCount]
     * </ul>
     * For example, if the result term is a list [a, b, c] then we return a singleton range [3].
     * If the result term is a list that has a variable for a tail, [a, b | xs] then we return a range [2, max].
     *
     * @param result           the result term of the query,
     *                         from which we try to deduce if the query expects a singleton set or not
     * @param unifier          the unifier
     * @param declarationCount the number of declarations found
     * @return the range of possible query result set sizes
     */
    private static Tuple2<Integer, Integer> resultSize(ITerm result, IUniDisunifier unifier, int declarationCount) {
        // @formatter:off
        final AtomicInteger min = new AtomicInteger(0);
        return M.<Tuple2<Integer, Integer>>list(ListTerms.casesFix(
            (m, cons) -> {
                // Increment the minimum
                min.incrementAndGet();
                return m.apply((IListTerm) unifier.findTerm(cons.getTail()));
            },
            (m, nil) -> Tuple2.of(min.get(), min.get()),
            (m, var) -> Tuple2.of(min.get(), declarationCount)
        )).match(result, unifier).orElse(Tuple2.of(0, declarationCount));
        // @formatter:on
    }


    @FunctionalInterface
    interface F1<A0, R> {
        R apply(A0 a0) throws ResolutionException, InterruptedException;
    }

}
