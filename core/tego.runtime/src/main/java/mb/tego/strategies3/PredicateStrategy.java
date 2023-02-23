package mb.tego.strategies3;


import mb.tego.sequences.Computation;
import mb.tego.sequences.Seq;
import mb.tego.strategies3.StrategyExt;
import mb.tego.strategies3.runtime.TegoEngine;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;

/**
 * A predicate strategy that can be used to adapt a predicate call with one argument as a strategy.
 * The strategy returns the input if the predicate succeeds; otherwise, the strategy fails.
 * <p>
 * Use {@link StrategyExt#pred}.
 *
 * @param <T> the type of input/output (invariant)
 */
@FunctionalInterface
public interface PredicateStrategy<T> extends Strategy<T, T> {
    @Override
    default Seq<T> evalInternal(TegoEngine engine, T input) {
        // Using fromOptional instead of fromNullable to allow `null` to be used as the value for `input`
        return Computation.from(() -> test(input) ? Optional.of(input) : Optional.empty());
    }

    boolean test(T input);
}
