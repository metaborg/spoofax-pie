package mb.tego.strategies;

import mb.tego.strategies.runtime.TegoEngine;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A predicate strategy that can be used to adapt a predicate call with two arguments as a strategy.
 * The strategy returns the input if the predicate succeeds; otherwise, the strategy fails.
 * <p>
 * Use {@link StrategyExt#pred}.
 *
 * @param <A1> the type of the first argument (contravariant)
 * @param <T> the type of input/output (invariant)
 */
@FunctionalInterface
public interface PredicateStrategy1<A1, T> extends Strategy1<A1, T, @Nullable T> {
    @Override
    default @Nullable T evalInternal(TegoEngine engine, A1 arg1, T input) {
        return test(input, arg1) ? input : null;
    }

    boolean test(T input, A1 arg1);
}
