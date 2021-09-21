package mb.statix.strategies;

import mb.statix.strategies.runtime.TegoEngine;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A predicate strategy that can be used to adapt a predicate call with two arguments as a strategy.
 * The strategy returns the input if the predicate succeeds; otherwise, the strategy fails.
 *
 * Use {@link StrategyExt#pred}.
 *
 * @param <CTX> the type of context (invariant)
 * @param <A1> the type of the first argument (contravariant)
 * @param <T> the type of input/output (invariant)
 */
@FunctionalInterface
public interface PredicateStrategy1<CTX, A1, T> extends Strategy1<CTX, A1, T, @Nullable T> {
    @Override
    default @Nullable T evalInternal(TegoEngine engine, CTX ctx, A1 arg1, T input) {
        return test(input, arg1) ? input : null;
    }

    boolean test(T input, A1 arg1);
}
