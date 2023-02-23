package mb.tego.strategies3;


import mb.tego.sequences.Seq;
import mb.tego.strategies3.StrategyExt;
import mb.tego.strategies3.runtime.TegoEngine;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Abstract class for a named strategy.
 * <p>
 * If the strategy is named, create a class extending {@link NamedStrategy}.
 * If the strategy is anonymous, create a lambda implementing {@link Strategy}.
 * <p>
 * Use {@link StrategyExt#def}.
 *
 * @param <T> the type of input (contravariant)
 * @param <R> the type of output (covariant)
 */
public abstract class NamedStrategy<T, R> implements Strategy<T, R> {

    @Override public abstract Seq<R> evalInternal(TegoEngine engine, T input);

    @Override public abstract String getName();

    @Override public boolean isAnonymous() { return false; }

    @Override public StringBuilder writeTo(StringBuilder sb) { sb.append(getName()); return sb; }

    @Override public final String toString() { return writeTo(new StringBuilder()).toString(); }
}
