package mb.statix.strategies;

import mb.statix.strategies.runtime.TegoEngine;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A predicate strategy that can be used to adapt a predicate call with one argument as a strategy.
 * The strategy returns the input if the predicate succeeds; otherwise, the strategy fails.
 *
 * Use {@link StrategyExt#pred}.
 *
 * @param <CTX> the type of context (invariant)
 * @param <T> the type of input/output (invariant)
 */
@FunctionalInterface
public interface PredicateStrategy<CTX, T> extends Strategy<CTX, T, @Nullable T> {
    @Override
    default @Nullable T evalInternal(TegoEngine engine, CTX ctx, T input) {
        return test(input) ? input : null;
    }

    boolean test(T input);
}
