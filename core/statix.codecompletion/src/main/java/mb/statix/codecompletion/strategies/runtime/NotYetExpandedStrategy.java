package mb.statix.codecompletion.strategies.runtime;

import mb.statix.codecompletion.CCSolverState;
import mb.statix.codecompletion.SolverState;
import mb.statix.constraints.CUser;
import mb.tego.strategies.NamedStrategy1;
import mb.tego.strategies.runtime.TegoEngine;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class NotYetExpandedStrategy extends NamedStrategy1<CUser, CCSolverState, @Nullable CCSolverState> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final NotYetExpandedStrategy instance = new NotYetExpandedStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static NotYetExpandedStrategy getInstance() { return (NotYetExpandedStrategy)instance; }

    private NotYetExpandedStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    @SuppressWarnings("UnnecessaryLocalVariable")
    @Override
    public @Nullable CCSolverState evalInternal(
        TegoEngine engine,
        CUser constraint,
        CCSolverState input
    ) {
        return eval(engine, constraint, input);
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    public static @Nullable CCSolverState eval(
        TegoEngine engine,
        CUser constraint,
        CCSolverState input
    ) {
        return checkNotYetExpanded(input, constraint) ? input : null;
    }

    private static boolean checkNotYetExpanded(CCSolverState state, CUser constraint) {
        boolean alreadyExpanded = state.getExpanded().contains(constraint.name());
        if (alreadyExpanded) {
            System.out.println("Constraint was expanded before: " + constraint);
        }
        return !alreadyExpanded;
    }

    @Override
    public String getName() {
        return "containsVar";
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    @Override
    public String getParamName(int index) {
        switch (index) {
            case 0: return "constraint";
            default: return super.getParamName(index);
        }
    }

}
