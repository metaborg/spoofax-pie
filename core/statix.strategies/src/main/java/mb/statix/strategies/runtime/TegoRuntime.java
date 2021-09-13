package mb.statix.strategies.runtime;

import mb.statix.sequences.Seq;
import mb.statix.strategies.Strategy;
import mb.statix.strategies.Strategy1;
import mb.statix.strategies.Strategy2;
import mb.statix.strategies.Strategy3;
import mb.statix.strategies.StrategyDecl;

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
     * @param ctx the context
     * @param args the arguments
     * @param input the input argument
     * @return the lazy sequence of results; or an empty sequence if the strategy failed
     */
    Seq<?> eval(StrategyDecl strategy, Object ctx, Object[] args, Object input);
//        // Call the right overload of `run` if we know this kind of strategy
//        if (strategy instanceof Strategy) {
//            final Strategy<? super Object, ? super Object, ?> strategy0
//                = (Strategy<? super Object, ? super Object, ?>)strategy;
//            if (args.length != 0) throw new IllegalArgumentException("Expected 0 arguments, got " + args.length + ".");
//            return strategy0.eval(ctx, input);
//        }
//        if (strategy instanceof Strategy1) {
//            final Strategy1<? super Object, ? super Object, ? super Object, ?> strategy1
//                = (Strategy1<? super Object, ? super Object, ? super Object, ?>)strategy;
//            if (args.length != 1) throw new IllegalArgumentException("Expected 1 arguments, got " + args.length + ".");
//            return strategy1.eval(ctx, args[0], input);
//        }
//        if (strategy instanceof Strategy2) {
//            final Strategy2<? super Object, ? super Object, ? super Object, ? super Object, ?> strategy2
//                = (Strategy2<? super Object, ? super Object, ? super Object, ? super Object, ?>)strategy;
//            if (args.length != 2) throw new IllegalArgumentException("Expected 2 arguments, got " + args.length + ".");
//            return strategy2.eval(ctx, args[0], args[1], input);
//        }
//        if (strategy instanceof Strategy3) {
//            final Strategy3<? super Object, ? super Object, ? super Object, ? super Object, ? super Object, ?> strategy3
//                = (Strategy3<? super Object, ? super Object, ? super Object, ? super Object, ? super Object, ?>)strategy;
//            if (args.length != 3) throw new IllegalArgumentException("Expected 3 arguments, got " + args.length + ".");
//            return strategy3.eval(ctx, args[0], args[1], args[2], input);
//        }
//        //
//        return strategy.eval(ctx, args, input);
//    }

    /**
     * Evaluates the strategy.
     *
     * @param strategy the strategy
     * @param ctx the context
     * @param input the input argument
     * @return the lazy sequence of results; or an empty sequence if the strategy failed
     */
    <CTX, T, R> Seq<R> eval(Strategy<CTX, T, R> strategy, CTX ctx, T input);

    /**
     * Evaluates the strategy.
     *
     * @param strategy the strategy
     * @param ctx the context
     * @param arg1 the first argument
     * @param input the input argument
     * @return the lazy sequence of results; or an empty sequence if the strategy failed
     */
    <CTX, A1, T, R> Seq<R> eval(Strategy1<CTX, A1, T, R> strategy, CTX ctx, A1 arg1, T input);

    /**
     * Evaluates the strategy.
     *
     * @param strategy the strategy
     * @param ctx the context
     * @param arg1 the first argument
     * @param arg2 the second argument
     * @param input the input argument
     * @return the lazy sequence of results; or an empty sequence if the strategy failed
     */
    <CTX, A1, A2, T, R> Seq<R> eval(Strategy2<CTX, A1, A2, T, R> strategy, CTX ctx, A1 arg1, A2 arg2, T input);

    /**
     * Evaluates the strategy.
     *
     * @param strategy the strategy
     * @param ctx the context
     * @param arg1 the first argument
     * @param arg2 the second argument
     * @param arg3 the third argument
     * @param input the input argument
     * @return the lazy sequence of results; or an empty sequence if the strategy failed
     */
    <CTX, A1, A2, A3, T, R> Seq<R> eval(Strategy3<CTX, A1, A2, A3, T, R> strategy, CTX ctx, A1 arg1, A2 arg2, A3 arg3, T input);

}
