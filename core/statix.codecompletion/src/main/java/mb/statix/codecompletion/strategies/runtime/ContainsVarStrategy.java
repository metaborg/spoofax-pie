package mb.statix.codecompletion.strategies.runtime;

import com.google.common.collect.ImmutableMap;
import mb.nabl2.terms.ITermVar;
import mb.statix.SolverContext;
import mb.statix.SolverState;
import mb.statix.constraints.messages.IMessage;
import mb.statix.sequences.Seq;
import mb.statix.solver.IConstraint;
import mb.statix.strategies.NamedStrategy1;
import mb.statix.strategies.NamedStrategy2;
import mb.statix.strategies.Strategy;
import mb.statix.strategies.Strategy1;
import mb.statix.strategies.runtime.AssertThatStrategy;
import mb.statix.strategies.runtime.FlatMapStrategy;
import mb.statix.strategies.runtime.NotStrategy;
import mb.statix.strategies.runtime.TegoEngine;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static mb.statix.utils.CollectionUtils.containsAny;

public final class ContainsVarStrategy extends NamedStrategy2<SolverContext, ITermVar, IConstraint, SolverState, @Nullable SolverState> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final ContainsVarStrategy instance = new ContainsVarStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static ContainsVarStrategy getInstance() { return (ContainsVarStrategy)instance; }

    private ContainsVarStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    @SuppressWarnings("UnnecessaryLocalVariable")
    @Override
    public @Nullable SolverState evalInternal(
        TegoEngine engine,
        SolverContext ctx,
        ITermVar v,
        IConstraint constraint,
        SolverState input
    ) {
        return eval(engine, ctx, v, constraint, input);
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    public static @Nullable SolverState eval(
        TegoEngine engine,
        SolverContext ctx,
        ITermVar v,
        IConstraint constraint,
        SolverState input
    ) {
        return containsAnyVar(Collections.singletonList(v), constraint, input) ? input : null;
    }

    private static boolean containsAnyVar(Collection<ITermVar> vars, IConstraint constraint, SolverState state) {
        @Nullable final ImmutableMap<ITermVar, ITermVar> existentials = state.getExistentials();
        final ArrayList<ITermVar> projectedVars = new ArrayList<>(vars.size());
        if(existentials != null) {
            for(ITermVar var : vars) {
                @Nullable ITermVar projected = existentials.get(var);
                if(projected != null) {
                    projectedVars.add(projected);
                } else {
                    projectedVars.add(var);
                }
            }
        } else {
            projectedVars.addAll(vars);
        }
        // We use the unifier to get all the variables in each of the argument to the constraint
        // (or the constraint argument itself when there where no variables and the argument is a term var)
        // and see if any match the var we're looking for.
        for(ITermVar arg : constraint.getVars()) {
            final io.usethesource.capsule.Set.Immutable<ITermVar> constraintVars = state.getState().unifier().getVars(arg);
            final boolean match = !constraintVars.isEmpty() ? containsAny(constraintVars, projectedVars) : projectedVars.contains(arg);
            if(match) return true;
        }
        return false;
    }

    private static boolean containsVar(ITermVar var, IConstraint constraint, SolverState state) {
        return containsAnyVar(Collections.singletonList(var), constraint, state);
    }

    @Override
    public String getName() {
        return "containsVar";
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    @Override
    public String getParamName(int index) {
        switch (index) {
            case 0: return "v";
            case 1: return "constraint";
            default: return super.getParamName(index);
        }
    }

}
