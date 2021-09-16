package mb.statix.codecompletion.strategies.runtime;

import mb.nabl2.terms.IApplTerm;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.ITermVar;
import mb.statix.SolverContext;
import mb.statix.SolverState;
import mb.statix.sequences.Seq;
import mb.statix.strategies.NamedStrategy1;
import mb.statix.strategies.NamedStrategy2;
import mb.statix.strategies.runtime.TegoEngine;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

public final class ExpandInjectionStrategy extends NamedStrategy2<SolverContext, ITermVar, Set<String>, SolverState, SolverState> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final ExpandInjectionStrategy instance = new ExpandInjectionStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static ExpandInjectionStrategy getInstance() { return (ExpandInjectionStrategy)instance; }

    private ExpandInjectionStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    @Override
    public Seq<SolverState> evalInternal(
        TegoEngine engine,
        SolverContext ctx,
        ITermVar v,
        Set<String> visitedInjections,
        SolverState input
    ) {
        return eval(engine, ctx, v, visitedInjections, input);
    }

    public static Seq<SolverState> eval(
        TegoEngine engine,
        SolverContext ctx,
        ITermVar v,
        Set<String> visitedInjections,
        SolverState input
    ) {
        final CompleteStrategy complete = CompleteStrategy.getInstance();

        // Project the term
        final ITerm root = input.project(v);

        // Breath-first search for injections (and other things we want to expand)
        final Deque<ITerm> queue = new ArrayDeque<ITerm>();
        queue.push(root);

        while (!queue.isEmpty()) {
            final ITerm term = queue.remove();

            // Ensure it is an injection application with one argument which is a variable
            if (!(term instanceof IApplTerm)) continue;
            final IApplTerm injTerm = (IApplTerm)term;

            if (injTerm.getArgs().size() == 1) {
                final ITerm injArg = injTerm.getArgs().get(0);
                if (injArg instanceof ITermVar && ctx.getIsInjPredicate().test(term)) {
                    // The term is an injection
                    // Ensure the injection was not already visited
                    final String injName = injTerm.getOp();
                    final ITermVar injArgVar = (ITermVar)injArg;
                    if(!visitedInjections.contains(injName)) {
                        final Set<String> newVisitedInjections = setWithElement(visitedInjections, injName);

                        // Complete the injection
                        return engine.eval(complete, ctx, injArgVar, newVisitedInjections, input);

                        // TODO: Get the set of visited injections back and use it
                    }
                }
            }
            // The term was rejected. Add its children to the queue.
            if (injTerm.getArgs().size() != 1) {
                queue.addAll(injTerm.getArgs());
            }
        }

        // No injection completed.
        return Seq.of();
    }

    @Override
    public String getName() {
        return "expandInjection";
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    @Override
    public String getParamName(int index) {
        switch (index) {
            case 0: return "v";
            case 1: return "visitedInjections";
            default: return super.getParamName(index);
        }
    }

    /**
     * Creates a new set of the given set and thr given element.
     *
     * @param set     the set
     * @param element the element to add
     * @param <T>     the type of elements
     * @return the new set, which is a union of the original set and the singleton set of the given element
     */
    private static <T> Set<T> setWithElement(Set<T> set, T element) {
        // TODO: Use a set more optimized for immutable operations
        final HashSet<T> newSet = new HashSet<>(set);
        newSet.add(element);
        return Collections.unmodifiableSet(newSet);
    }

}
