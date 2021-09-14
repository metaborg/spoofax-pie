package mb.statix.patterns;

import mb.statix.tuples.Pair;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A pattern for a tuple with two components.
 *
 * @param <CTX> the type of context (invariant)
 * @param <R1> the type of the first component (contravariant)
 * @param <R2> the type of the second component (contravariant)
 */
public final class PairPattern<CTX, T1, T2, R1, R2> implements Pattern<CTX, Object, Pair<R1, R2>> {

    private final Pattern<CTX, T1, R1> p1;
    private final Pattern<CTX, T2, R2> p2;

    /**
     * Initializes a new instance of the {@link PairPattern} class.
     *
     * @param p1 the pattern of the first component
     * @param p2 the pattern of the second component
     */
    public PairPattern(Pattern<CTX, T1, R1> p1, Pattern<CTX, T2, R2> p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    @Override
    public boolean match(CTX ctx, @Nullable Object input) {
        if (!(input instanceof Pair)) return false;
        @SuppressWarnings("unchecked")
        final Pair<@Nullable T1, @Nullable T2> tuple = (Pair<T1, T2>)input;
        return p1.match(ctx, tuple.component1())
            && p2.match(ctx, tuple.component2());
    }

}
