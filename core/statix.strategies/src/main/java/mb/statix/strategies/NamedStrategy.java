package mb.statix.strategies;

import mb.statix.sequences.Seq;

/**
 * Abstract class for a named strategy.
 *
 * If the strategy is named, create a class extending {@link NamedStrategy}.
 * If the strategy is anonymous, create a lambda implementing {@link Strategy}.
 *
 * @param <CTX> the type of context (invariant)
 * @param <T> the type of input (contravariant)
 * @param <R> the type of output (covariant)
 */
public abstract class NamedStrategy<CTX, T, R> implements Strategy<CTX, T, R> {

    @Override public abstract Seq<R> eval(CTX ctx, T input);

    @Override public abstract String getName();

    @Override public boolean isAnonymous() { return false; }

    @Override public StringBuilder writeTo(StringBuilder sb) { sb.append(getName()); return sb; }

    @Override public final String toString() { return writeTo(new StringBuilder()).toString(); }
}
