package mb.statix.codecompletion.strategies.runtime;


import io.usethesource.capsule.Map;
import mb.nabl2.terms.ITermVar;
import mb.statix.codecompletion.CCSolverState;
import mb.statix.solver.IConstraint;
import mb.tego.strategies.NamedStrategy2;
import mb.tego.strategies.runtime.TegoEngine;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collection;

import static mb.tego.utils.CollectionUtils.containsAny;

public final class ContainsAnyVarStrategy extends NamedStrategy2<Collection<ITermVar>, IConstraint, CCSolverState, @Nullable CCSolverState> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final ContainsAnyVarStrategy instance = new ContainsAnyVarStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static ContainsAnyVarStrategy getInstance() { return (ContainsAnyVarStrategy)instance; }

    private ContainsAnyVarStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    @SuppressWarnings("UnnecessaryLocalVariable")
    @Override
    public @Nullable CCSolverState evalInternal(
        TegoEngine engine,
        Collection<ITermVar> vars,
        IConstraint constraint,
        CCSolverState input
    ) {
        return eval(engine, vars, constraint, input);
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    public static @Nullable CCSolverState eval(
        TegoEngine engine,
        Collection<ITermVar> vars,
        IConstraint constraint,
        CCSolverState input
    ) {
        return containsAnyVar(vars, constraint, input) ? input : null;
    }

    private static boolean containsAnyVar(Collection<ITermVar> vars, IConstraint constraint, CCSolverState state) {
        final Map.@Nullable Immutable<ITermVar, ITermVar> existentials = state.getExistentials();
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

    @Override
    public String getName() {
        return "containsAnyVar";
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    @Override
    public String getParamName(int index) {
        switch (index) {
            case 0: return "vars";
            case 1: return "constraint";
            default: return super.getParamName(index);
        }
    }

}
