package mb.statix.codecompletion.strategies.runtime;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import mb.nabl2.terms.IListTerm;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.ITermVar;
import mb.nabl2.terms.ListTerms;
import mb.nabl2.terms.unification.ud.IUniDisunifier;
import mb.tego.sequences.Seq;
import mb.statix.codecompletion.SelectedConstraintSolverState;
import mb.statix.codecompletion.SolverContext;
import mb.statix.codecompletion.SolverState;
import mb.statix.constraints.CAstId;
import mb.statix.constraints.CEqual;
import mb.statix.constraints.CInequal;
import mb.statix.constraints.CResolveQuery;
import mb.statix.generator.scopegraph.DataWF;
import mb.statix.generator.scopegraph.Env;
import mb.statix.generator.scopegraph.Match;
import mb.statix.generator.scopegraph.NameResolution;
import mb.statix.generator.strategy.ResolveDataWF;
import mb.scopegraph.oopsla20.reference.EdgeOrData;
import mb.scopegraph.oopsla20.reference.LabelOrder;
import mb.scopegraph.oopsla20.reference.LabelWF;
import mb.scopegraph.oopsla20.reference.ResolutionException;
import mb.statix.scopegraph.Scope;
import mb.statix.solver.IConstraint;
import mb.statix.solver.IState;
import mb.statix.solver.completeness.ICompleteness;
import mb.scopegraph.oopsla20.reference.RegExpLabelWF;
import mb.scopegraph.oopsla20.reference.RelationLabelOrder;
import mb.statix.spec.Spec;
import mb.statix.spoofax.StatixTerms;
import mb.tego.strategies.NamedStrategy2;
import mb.tego.strategies.runtime.TegoEngine;
import mb.tego.utils.StreamUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.util.functions.Predicate2;
import org.metaborg.util.iterators.Iterables2;
import org.metaborg.util.optionals.Optionals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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
public final class ExpandQueryStrategyOld extends NamedStrategy2<SolverContext, ITermVar, SelectedConstraintSolverState<CResolveQuery>, Seq<SolverState>> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final ExpandQueryStrategyOld instance = new ExpandQueryStrategyOld();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static ExpandQueryStrategyOld getInstance() { return (ExpandQueryStrategyOld)instance; }

    private ExpandQueryStrategyOld() { /* Prevent instantiation. Use getInstance(). */ }

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
    public Seq<SolverState> evalInternal(
        TegoEngine engine,
        SolverContext ctx,
        ITermVar v,
        SelectedConstraintSolverState<CResolveQuery> input
    ) {
        return eval(engine, ctx, v, input);
    }

    public static Seq<SolverState> eval(
        TegoEngine engine,
        SolverContext ctx,
        ITermVar v,
        SelectedConstraintSolverState<CResolveQuery> input
    ) {
        final CResolveQuery query = input.getSelected();

        engine.log(instance, "Expand query: {}", query);

        final IState.Immutable state = input.getState();
        final IUniDisunifier unifier = state.unifier();

        // Find the scope
        @Nullable final Scope scope = Scope.matcher().match(query.scopeTerm(), unifier).orElse(null);
        if(scope == null) throw new IllegalArgumentException("cannot resolve query: no scope");

        // Determine data equivalence (either: true, false, or null when it could not be determined)
        @Nullable final Boolean isAlways;
        try {
            isAlways = query.min().getDataEquiv().isAlways().orElse(null);
        } catch(InterruptedException e) {
            throw new RuntimeException(e);
        }
        if(isAlways == null) {
            throw new IllegalArgumentException("cannot resolve query: cannot decide data equivalence");
        }

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
        final int declarationCount = countDeclarations(nameResolution, scope);
        engine.log(instance, "  ▶ found {} declarations", declarationCount);

        // For each declaration:
        final ArrayList<SolverState> output = new ArrayList<>();
        for (int i = 0; i < declarationCount; i++) {
            final List<SolverState> newStates = expandResolution(engine, input.getSpec(), query, input.withoutSelected(),
                unifier, nameResolution, scope, i);
            engine.log(instance, "  ▶ added {} possible states", newStates.size());
            output.addAll(newStates);
        }
        engine.log(instance, "▶ expanded {} declarations into {} possible states", declarationCount, output.size());

        @Nullable final ITermVar focusVar = ctx.getFocusVar();
        if (focusVar != null && engine.isLogEnabled(instance)) {
            for(SolverState s : output) {
                engine.log(instance, "- {}", s.project(focusVar));
            }
        }

        return Seq.from(output);
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
    private static List<SolverState> expandResolution(
        TegoEngine engine,
        Spec spec,
        CResolveQuery query,
        SolverState inputState,
        IUniDisunifier unifier,
        NameResolution<Scope, ITerm, ITerm, CEqual> nameResolution,
        Scope scope,
        int index
    ) {
        final Env<Scope, ITerm, ITerm, CEqual> env = resolveByIndex(nameResolution, scope, index);

        engine.log(instance, "  ▶ ▶ declaration #{}: {} matches", index, env.matches.size());

        // No matches, so no results
        if(env.matches.isEmpty()) {
            return ImmutableList.of();
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

        final List<SolverState> newStates = new ArrayList<>();
        for (List<Match<Scope, ITerm, ITerm, CEqual>> optMatchGroup : optMatchGroups) {
            engine.log(instance, "  ▶ ▶ ▶ match group {}", optMatchGroup);
            // Determine the range of sizes the query result set can be
            final Range<Integer> sizes = resultSize(query.resultTerm(), unifier, optMatchGroup.size());

            engine.log(instance, "  ▶ ▶ ▶ sizes {}", sizes);

            // For each possible size:
            final List<SolverState> states = expandResolutionSets(
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
    ) {
        // Find all possible declarations the resolution could resolve to.
        final AtomicInteger count = new AtomicInteger(1);
        try {
            nameResolution.resolve(scope, () -> {
                count.incrementAndGet();
                return false;
            });
        } catch(ResolutionException e) {
            throw new IllegalArgumentException("cannot resolve query: delayed on " + e.getMessage());
        } catch(InterruptedException e) {
            throw new RuntimeException(e);
        }
        return count.get();
    }

    private static Env<Scope, ITerm, ITerm, CEqual> resolveByIndex(
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
     * @param spec the spec
     * @param query the query
     * @param state the input state
     * @param sizes the range of sizes
     * @param optMatches the optional matches
     * @param reqMatches the required matches
     * @param reqRejects the required rejects
     * @return a list of new solver states
     */
    private static List<SolverState> expandResolutionSets(
        TegoEngine engine,
        Spec spec,
        CResolveQuery query,
        SolverState state,
        Range<Integer> sizes,
        Collection<Match<Scope, ITerm, ITerm, CEqual>> optMatches,
        Collection<Match<Scope, ITerm, ITerm, CEqual>> reqMatches,
        Collection<Match<Scope, ITerm, ITerm, CEqual>> reqRejects
    ) {
        // FIXME: Query should not return shadowed declarations, such as those that are further away
        // FIXME: Support queries that return multiple results
        return IntStream.rangeClosed(sizes.lowerEndpoint(), sizes.upperEndpoint()).mapToObj(size ->
        {
            final Stream<Collection<Match<Scope, ITerm, ITerm, CEqual>>> subsets = StreamUtils.subsetsOfSize(optMatches.stream(), size);
            final List<Collection<Match<Scope, ITerm, ITerm, CEqual>>> subsetsList = subsets.collect(Collectors.toList());
            engine.log(instance, "  ▶ ▶ ▶ for size {}: {}", size, subsetsList);
            return subsetsList.stream().map(matches ->
                updateSolverState(
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
     * Creates a new solver state for the given matches.
     *
     * @param spec the spec
     * @param query the query
     * @param state the input state
     * @param optMatches the accepted optional matches
     * @param optRejects the rejected optional matches
     * @param reqMatches the required matches
     * @param reqRejects the required rejects
     * @return the new solver state
     */
    private static SolverState updateSolverState(
        Spec spec,
        CResolveQuery query,
        SolverState state,
        Collection<Match<Scope, ITerm, ITerm, CEqual>> optMatches,
        Collection<Match<Scope, ITerm, ITerm, CEqual>> optRejects,
        Collection<Match<Scope, ITerm, ITerm, CEqual>> reqMatches,
        Collection<Match<Scope, ITerm, ITerm, CEqual>> reqRejects
    ) {
        // Build a new environment with all the matches and rejects.
        final Env.Builder<Scope, ITerm, ITerm, CEqual> subEnvBuilder = Env.builder();
        optMatches.forEach(subEnvBuilder::match);
        reqMatches.forEach(subEnvBuilder::match);
        optRejects.forEach(subEnvBuilder::reject);
        reqRejects.forEach(subEnvBuilder::reject);
        final Env<Scope, ITerm, ITerm, CEqual> subEnv = subEnvBuilder.build();

        // Build the list of constraints to add
        final ImmutableList.Builder<IConstraint> addConstraints = ImmutableList.builder();

        // The explicated match path must match the query result term
        final List<ITerm> pathTerms = subEnv.matches.stream().map(m -> StatixTerms.pathToTerm(m.path, spec.dataLabels()))
            .collect(ImmutableList.toImmutableList());
        addConstraints.add(new CEqual(B.newList(pathTerms), query.resultTerm(), query));
        subEnv.matches.stream().flatMap(m -> Optionals.stream(m.condition)).forEach(addConstraints::add);
        subEnv.rejects.stream().flatMap(m -> Optionals.stream(m.condition)).forEach(condition -> addConstraints.add(
            new CInequal(ImmutableSet.of(), condition.term1(), condition.term2(),
                condition.cause().orElse(null), condition.message().orElse(null))
            )
        );

        // Build the list of constraints to remove
        final Iterable<IConstraint> remConstraints = Iterables2.singleton(query);

        // Update the given state with the added and removed constraints
        return state.withUpdatedConstraints(addConstraints.build(), remConstraints)
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
    private static ImmutableList<IConstraint> removeTermIdConstraints(ImmutableList<IConstraint> constraints) {
        return ImmutableList.copyOf(Collections2.filter(constraints, c -> !(c instanceof CAstId)));
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
     * @param result the result term of the query,
     *               from which we try to deduce if the query expects a singleton set or not
     * @param unifier the unifier
     * @param declarationCount the number of declarations found
     * @return the range of possible query result set sizes
     */
    private static Range<Integer> resultSize(ITerm result, IUniDisunifier unifier, int declarationCount) {
        // @formatter:off
        final AtomicInteger min = new AtomicInteger(0);
        return M.<Range<Integer>>list(ListTerms.casesFix(
            (m, cons) -> {
                // Increment the minimum
                min.incrementAndGet();
                return m.apply((IListTerm) unifier.findTerm(cons.getTail()));
            },
            (m, nil) -> Range.singleton(min.get()),
            (m, var) -> Range.closed(min.get(), declarationCount)
        )).match(result, unifier).orElse(Range.closed(0, declarationCount));
        // @formatter:on
    }

}
