package mb.statix.strategies;

import mb.statix.strategies.runtime.TegoEngine;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An adapter strategy that can be used to adapt a function call with four arguments as a strategy.
 *
 * Use {@link StrategyExt#pred}.
 *
 * @param <A1> the type of the first argument (contravariant)
 * @param <A2> the type of the second argument (contravariant)
 * @param <A3> the type of the third argument (contravariant)
 * @param <T> the type of input/output (invariant)
 */
@FunctionalInterface
public interface PredicateStrategy3<A1, A2, A3, T> extends Strategy3<A1, A2, A3, T, @Nullable T> {
    @Override
    default @Nullable T evalInternal(TegoEngine engine, A1 arg1, A2 arg2, A3 arg3, T input) {
        return test(input, arg1, arg2, arg3) ? input : null;
    }

    boolean test(T input, A1 arg1, A2 arg2, A3 arg3);
}
