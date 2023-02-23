package mb.tego.strategies3;


import mb.tego.sequences.Computation;
import mb.tego.sequences.Seq;
import mb.tego.strategies3.StrategyExt;
import mb.tego.strategies3.runtime.TegoEngine;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;

/**
 * An adapter strategy that can be used to adapt a function call with four arguments as a strategy.
 * <p>
 * Use {@link StrategyExt#pred}.
 *
 * @param <A1> the type of the first argument (contravariant)
 * @param <A2> the type of the second argument (contravariant)
 * @param <A3> the type of the third argument (contravariant)
 * @param <T> the type of input/output (invariant)
 */
@FunctionalInterface
public interface PredicateStrategy3<A1, A2, A3, T> extends Strategy3<A1, A2, A3, T, T> {
    @Override
    default Seq<T> evalInternal(TegoEngine engine, A1 arg1, A2 arg2, A3 arg3, T input) {
        // Using fromOptional instead of fromNullable to allow `null` to be used as the value for `input`
        return Computation.from(() -> test(input, arg1, arg2, arg3) ? Optional.of(input) : Optional.empty());
    }

    boolean test(T input, A1 arg1, A2 arg2, A3 arg3);
}
