package mb.statix.codecompletion.strategies.runtime;

import mb.nabl2.terms.ITermVar;
import mb.nabl2.terms.stratego.StrategoPlaceholders;
import mb.statix.codecompletion.CCSolverState;
import mb.statix.codecompletion.SolverState;
import mb.tego.strategies.NamedStrategy1;
import mb.tego.strategies.Strategy;
import mb.tego.strategies.runtime.TegoEngine;
import org.checkerframework.checker.nullness.qual.Nullable;

import static mb.tego.strategies.StrategyExt.*;
import static mb.tego.strategies.runtime.Strategies.*;

@SuppressWarnings("RedundantSuppression")
public final class FilterPlaceholderStrategy extends NamedStrategy1<ITermVar, CCSolverState, @Nullable CCSolverState> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final FilterPlaceholderStrategy instance = new FilterPlaceholderStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static FilterPlaceholderStrategy getInstance() { return (FilterPlaceholderStrategy)instance; }

    private FilterPlaceholderStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    @SuppressWarnings("UnnecessaryLocalVariable")
    @Override
    public @Nullable CCSolverState evalInternal(
        TegoEngine engine,
        ITermVar v,
        CCSolverState input
    ) {
        return eval(engine, v, input);
    }

    @SuppressWarnings({"UnnecessaryLocalVariable", "RedundantIfStatement"})
    public static @Nullable CCSolverState eval(
        TegoEngine engine,
        ITermVar v,
        CCSolverState input
    ) {
        // Tego:
        // def filterPlaceholder(v: ITermVar) SolverState -> SolverState?
        // = where(project(v); not(StrategoPlaceholders#isPlaceholder); not(is<ITermVar>))
        final Strategy<CCSolverState, @Nullable CCSolverState> s
            = where(seq(fun(CCSolverState::project).apply(v))
            .$(not(fun(StrategoPlaceholders::isPlaceholder)))
            .$(not(is(ITermVar.class)))
            .$());
        return engine.eval(s, input);
    }

    @Override
    public String getName() {
        return "filterPlaceholder";
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
