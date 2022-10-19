package mb.tego.strategies;

import mb.tego.strategies.runtime.TegoEngine;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A predicate strategy that can be used to adapt a predicate call with one argument as a strategy.
 * The strategy returns the input if the predicate succeeds; otherwise, the strategy fails.
 * <p>
 * Use {@link StrategyExt#pred}.
 *
 * @param <T> the type of input/output (invariant)
 */
@FunctionalInterface
public interface PredicateStrategy<T> extends Strategy<T, @Nullable T> {
    @Override
    default @Nullable T evalInternal(TegoEngine engine, T input) {
        return test(input) ? input : null;
    }

    boolean test(T input);
}
