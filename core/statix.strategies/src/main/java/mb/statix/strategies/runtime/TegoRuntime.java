package mb.statix.strategies.runtime;

import mb.statix.sequences.Seq;
import mb.statix.strategies.Strategy;
import mb.statix.strategies.Strategy1;
import mb.statix.strategies.Strategy2;
import mb.statix.strategies.Strategy3;
import mb.statix.strategies.StrategyDecl;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * The Tego runtime.
 *
 * The methods in this interface enable the evaluation of strategies.
 */
public interface TegoRuntime {

    /**
     * Evaluates the strategy.
     *
     * This is a trampoline method.
     *
     * @param strategy the strategy
     * @param args the arguments
     * @param input the input argument
     * @return the lazy sequence of results; or an empty sequence if the strategy failed
     */
    @Nullable Object eval(StrategyDecl strategy, Object[] args, Object input);

    /**
     * Evaluates the strategy.
     *
     * @param strategy the strategy
     * @param input the input argument
     * @return the lazy sequence of results; or an empty sequence if the strategy failed
     */
    <T, R> @Nullable R eval(Strategy<T, R> strategy, T input);

    /**
     * Evaluates the strategy.
     *
     * @param strategy the strategy
     * @param arg1 the first argument
     * @param input the input argument
     * @return the lazy sequence of results; or an empty sequence if the strategy failed
     */
    <A1, T, R> @Nullable R eval(Strategy1<A1, T, R> strategy, A1 arg1, T input);

    /**
     * Evaluates the strategy.
     *
     * @param strategy the strategy
     * @param arg1 the first argument
     * @param arg2 the second argument
     * @param input the input argument
     * @return the lazy sequence of results; or an empty sequence if the strategy failed
     */
    <A1, A2, T, R> @Nullable R eval(Strategy2<A1, A2, T, R> strategy, A1 arg1, A2 arg2, T input);

    /**
     * Evaluates the strategy.
     *
     * @param strategy the strategy
     * @param arg1 the first argument
     * @param arg2 the second argument
     * @param arg3 the third argument
     * @param input the input argument
     * @return the lazy sequence of results; or an empty sequence if the strategy failed
     */
    <A1, A2, A3, T, R> @Nullable R eval(Strategy3<A1, A2, A3, T, R> strategy, A1 arg1, A2 arg2, A3 arg3, T input);

}
