package mb.statix.strategies;

import mb.statix.sequences.Seq;

/**
 * Abstract class for a named strategy.
 *
 * If the strategy is named, create a class extending {@link NamedStrategy}.
 * If the strategy is anonymous, create a lambda implementing {@link Strategy}.
 *
 * @param <CTX> the type of context (invariant)
 * @param <A1> the type of the first argument (contravariant)
 * @param <A2> the type of the second argument (contravariant)
 * @param <A3> the type of the third argument (contravariant)
 * @param <T> the type of input (contravariant)
 * @param <R> the type of output (covariant)
 */
public abstract class NamedStrategy3<CTX, A1, A2, A3, T, R> implements Strategy3<CTX, A1, A2, A3, T, R> {

    @Override public abstract Seq<R> eval(CTX ctx, A1 arg1, A2 arg2, A3 arg3, T input);

    @Override public abstract String getName();

    @Override public boolean isAnonymous() { return false; }

    @Override public StringBuilder writeTo(StringBuilder sb) { sb.append(getName()); return sb; }

    @Override public final String toString() { return writeTo(new StringBuilder()).toString(); }
}
