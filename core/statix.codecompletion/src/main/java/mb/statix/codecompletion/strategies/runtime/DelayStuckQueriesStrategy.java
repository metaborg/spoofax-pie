package mb.statix.codecompletion.strategies.runtime;

import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.unification.ud.IUniDisunifier;
import mb.scopegraph.oopsla20.reference.EdgeOrData;
import mb.scopegraph.oopsla20.reference.IncompleteException;
import mb.scopegraph.oopsla20.reference.LabelOrder;
import mb.scopegraph.oopsla20.reference.LabelWF;
import mb.scopegraph.oopsla20.reference.RegExpLabelWF;
import mb.scopegraph.oopsla20.reference.RelationLabelOrder;
import mb.scopegraph.oopsla20.reference.ResolutionException;
import mb.statix.SolverContext;
import mb.statix.SolverState;
import mb.statix.constraints.CEqual;
import mb.statix.constraints.CResolveQuery;
import mb.statix.generator.scopegraph.DataWF;
import mb.statix.generator.scopegraph.NameResolution;
import mb.statix.generator.strategy.ResolveDataWF;
import mb.statix.scopegraph.Scope;
import mb.statix.sequences.Seq;
import mb.statix.solver.CriticalEdge;
import mb.statix.solver.Delay;
import mb.statix.solver.IConstraint;
import mb.statix.solver.IState;
import mb.statix.solver.completeness.ICompleteness;
import mb.statix.spec.Spec;
import mb.statix.strategies.NamedStrategy;
import mb.statix.strategies.runtime.TegoEngine;
import mb.statix.tuples.Pair;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.util.functions.Predicate2;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Delays stuck queries.
 */
public final class DelayStuckQueriesStrategy extends NamedStrategy<SolverContext, SolverState, Seq<SolverState>> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final DelayStuckQueriesStrategy instance = new DelayStuckQueriesStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static DelayStuckQueriesStrategy getInstance() { return instance; }

    private DelayStuckQueriesStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    @Override
    public String getName() {
        return "delayStuckQueries";
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    @Override
    public String getParamName(int index) {
        switch (index) {
            default: return super.getParamName(index);
        }
    }

    @Override
    public Seq<SolverState> evalInternal(TegoEngine engine, SolverContext ctx, SolverState input) {
        return eval(engine, ctx, input);
    }

    public static Seq<SolverState> eval(TegoEngine engine, SolverContext ctx, SolverState input) {
        final IState.Immutable state = input.getState();
        final ICompleteness.Immutable completeness = input.getCompleteness();

//        final java.util.Map<IConstraint, Delay> delays = Maps.newHashMap();
//        // TODO: Use sequences
//        input.getConstraints().stream().filter(c -> c instanceof CResolveQuery).map(c -> (CResolveQuery) c).forEach(q -> checkDelay(ctx.getSpec(), q, state, completeness).ifPresent(d -> {
//            delays.put(q, d);
//        }));

        final Map<IConstraint, Delay> delays;
        try {
            delays = Seq.from(input.getConstraints())
                .filterIsInstance(CResolveQuery.class)
                .mapPresent(q -> checkDelay(ctx.getSpec(), q, state, completeness).map(d -> Pair.of(q, d)))
                .collect(Collectors.toMap(Pair::component1, Pair::component2));
        } catch(InterruptedException e) {
            throw new RuntimeException(e);
        }

        return Seq.of(input.withDelays(delays.entrySet()));
    }

    private static Optional<Delay> checkDelay(Spec spec, CResolveQuery query, IState.Immutable state,
                                       ICompleteness.Immutable completeness) {
        final IUniDisunifier unifier = state.unifier();

        if(!unifier.isGround(query.scopeTerm())) {
            return Optional.of(Delay.ofVars(unifier.getVars(query.scopeTerm())));
        }
        @Nullable final Scope scope = Scope.matcher().match(query.scopeTerm(), unifier).orElse(null);
        if(scope == null) {
            return Optional.empty();
        }

        @Nullable final Boolean isAlways;
        try {
            isAlways = query.min().getDataEquiv().isAlways().orElse(null);
        } catch(InterruptedException e) {
            throw new RuntimeException(e);
        }
        if(isAlways == null) {
            return Optional.empty();
        }

        final LabelWF<ITerm> labelWF = RegExpLabelWF.of(query.filter().getLabelWF());
        final LabelOrder<ITerm> labelOrd = new RelationLabelOrder(query.min().getLabelOrder());
        final DataWF<ITerm, CEqual> dataWF = new ResolveDataWF(state, completeness, query.filter().getDataWF(), query);
        final Predicate2<Scope, EdgeOrData<ITerm>> isComplete =
            (s, l) -> completeness.isComplete(s, l, state.unifier());

        // @formatter:off
        final NameResolution<Scope, ITerm, ITerm, CEqual> nameResolution = new NameResolution<>(
            spec,
            state.scopeGraph(),
            spec.allLabels(),
            labelWF, labelOrd,
            dataWF, isAlways, isComplete);
        // @formatter:on

        try {
            nameResolution.resolve(scope, () -> false);
        } catch(IncompleteException e) {
            return Optional.of(Delay.ofCriticalEdge(CriticalEdge.of(e.scope(), e.label())));
        } catch(ResolutionException e) {
            throw new RuntimeException("Unexpected resolution exception.", e);
        } catch(InterruptedException e) {
            throw new RuntimeException(e);
        }

        return Optional.empty();
    }

}
