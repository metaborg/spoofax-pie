package mb.statix.codecompletion.strategies.runtime;

import mb.nabl2.terms.ITermVar;
import mb.statix.SolverContext;
import mb.statix.SolverState;
import mb.statix.sequences.Seq;
import mb.statix.strategies.NamedStrategy1;
import mb.statix.strategies.NamedStrategy2;
import mb.statix.strategies.runtime.TegoEngine;

import java.util.Set;

public final class ExpandAllPredicatesStrategy extends NamedStrategy1<SolverContext, ITermVar, SolverState, SolverState> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final ExpandAllPredicatesStrategy instance = new ExpandAllPredicatesStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static ExpandAllPredicatesStrategy getInstance() { return (ExpandAllPredicatesStrategy)instance; }

    private ExpandAllPredicatesStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    @Override
    public Seq<SolverState> evalInternal(
        TegoEngine engine,
        SolverContext solverContext,
        ITermVar v,
        SolverState input
    ) {
        return eval(engine, solverContext, v, input);
    }

    public static Seq<SolverState> eval(
        TegoEngine engine,
        SolverContext solverContext,
        ITermVar v,
        SolverState input
    ) {
        // Tego:
        // import io/usethesource/capsule::Set.Immutable
        //
        // def expandAllPredicates(v: ITermVar): SolverState -> [SolverState] =
        //     SolverState#withExpanded(Set.Immutable#of) |>
        //     repeat(
        //       limit(1, select(CUser::class, \(constraint: IConstraint) SolverState -> SolverState? :- state ->
        //           <containsVar(v, constraint) ; checkNotYetExpanded(constraint)> state
        //       \)) |>
        //       expandPredicate(v) |>
        //       assertValid(v)
        //     )

        // IR: (ANF)
        // import io/usethesource/capsule::Set.Immutable
        //
        // def expandAllPredicates(v: ITermVar): SolverState -> [SolverState] :- input ->
        //     let f1: Set.Immutable<String> = Set.Immutable#of in
        //     let s1: SolverState -> SolverState = SolverState#withExpanded(f1) in
        //     let r1: SolverState = __eval(s1, input) in
        //
        //     let c2: Class = CUser::class in
        //     let l2: (IConstraint) SolverState -> SolverState? = \(constraint: IConstraint) SolverState -> SolverState? :- state ->
        //         let s1_1: SolverState -> SolverState? = containsVar(v, constraint) in
        //         let s1_1': SolverState? -> SolverState? = __maybe(s1_1) in
        //         let r1_1: SolverState? = __eval(s1_1', state) in
        //         let s1_2: SolverState -> SolverState? = checkNotYetExpanded(constraint) in
        //         let s1_2': SolverState? -> SolverState? = __maybe(s1_2) in
        //         let r1_2: SolverState? = __eval(s1_2', state) in
        //         r1_2
        //     \ in
        //     let s2: SolverState -> [SolverState] = select(c2, l2) in
        //     let s3: SolverState -> [SolverState] = limit(1, s2) in
        //     let s4: SolverState -> [SolverState] = expandPredicate(v) in
        //     let s4': [SolverState] -> [SolverState] = __flatMap(s4) in
        //     let s4'': SolverState -> [SolverState] = __seq(s3, s4') in     // NOTE: __seq
        //     let s5: SolverState -> [SolverState] = assertValid(v) in
        //     let s5': [SolverState] -> [SolverState] = __flatMap(s5) in
        //     let s5'': SolverState -> [SolverState] = __seq(s4'', s5') in   // NOTE: __seq
        //     let s6: SolverState -> [SolverState] = repeat(s5'') in
        //     let s6': [SolverState] -> [SolverState] = __flatMap(s6) in
        //     let r6: [SolverState] = __eval(s6', r1) in
        //     r6
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getName() {
        return "expandAllPredicates";
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    @Override
    public String getParamName(int index) {
        switch (index) {
            case 0: return "v";
            default: return super.getParamName(index);
        }
    }

}
