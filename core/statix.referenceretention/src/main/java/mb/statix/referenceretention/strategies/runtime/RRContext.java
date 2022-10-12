package mb.statix.referenceretention.strategies.runtime;

import mb.nabl2.terms.ITerm;
import mb.tego.strategies.Strategy1;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * The context in which the reference retention is performed.
 */
public final class RRContext {

    private final Strategy1<ITerm, LockedReference, @Nullable ITerm> qualifyReferenceStrategy;

    /**
     * Initializes a new instance of the {@link RRContext} class.
     *
     * @param qualifyReferenceStrategy a strategy that qualifies a reference
     */
    public RRContext(Strategy1<ITerm, LockedReference, @Nullable ITerm> qualifyReferenceStrategy) {
        this.qualifyReferenceStrategy = qualifyReferenceStrategy;
    }

    /**
     * Gets the strategy for qualifying references.
     *
     * The strategy has signature {@code (context: ITerm) LockedReference -> ITerm?},
     * where the input term is the reference to qualify and the result is either
     * the qualified reference, or {@code null} if the strategy failed.
     *
     * @return the strategy
     */
    public Strategy1<ITerm, LockedReference, @Nullable ITerm> getQualifyReferenceStrategy() {
        return qualifyReferenceStrategy;
    }

}
