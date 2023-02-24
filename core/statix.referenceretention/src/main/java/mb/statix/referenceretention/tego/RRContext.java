package mb.statix.referenceretention.tego;

import mb.nabl2.terms.IListTerm;
import mb.nabl2.terms.IStringTerm;
import mb.nabl2.terms.ITerm;
import mb.statix.constraints.messages.IMessage;
import mb.statix.solver.IConstraint;
import mb.statix.solver.persistent.SolverResult;
import mb.tego.strategies.Strategy2;
import mb.tego.strategies.Strategy3;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.Map;

/**
 * The context in which the reference retention is performed.
 */
public final class RRContext {

    private final Strategy2</* ctx */ IListTerm, /* sortName */ IStringTerm, /* term */ ITerm, /* result */ @Nullable ITerm> qualifyReferenceStrategy;
    private final Collection<Map.Entry<IConstraint, IMessage>> allowedErrors;

    /**
     * Initializes a new instance of the {@link RRContext} class.
     *
     * @param qualifyReferenceStrategy a strategy that qualifies a reference
     * @param allowedErrors a collection of constraints/message pairs that are allowed
     */
    public RRContext(
        Strategy2</* ctx */ IListTerm, /* sortName */ IStringTerm, /* term */ ITerm, /* result */ @Nullable ITerm> qualifyReferenceStrategy,
        Collection<Map.Entry<IConstraint, IMessage>> allowedErrors
    ) {
        this.qualifyReferenceStrategy = qualifyReferenceStrategy;
        this.allowedErrors = allowedErrors;
    }

    /**
     * Gets the strategy for qualifying references.
     * <p>
     * The strategy has signature {@code (context: ITerm) LockedReference -> ITerm?},
     * where the input term is the reference to qualify and the result is either
     * the qualified reference, or {@code null} if the strategy failed.
     *
     * @return the strategy
     */
    public Strategy2</* ctx */ IListTerm, /* sortName */ IStringTerm, /* term */ ITerm, /* result */ @Nullable ITerm> getQualifyReferenceStrategy() {
        return qualifyReferenceStrategy;
    }

    /**
     * The allowed errors.
     * <p>
     * This is used to know which errors existed in the project before the operation was invoked.
     *
     * @return a collection of constraints/message pairs that are allowed
     */
    public Collection<Map.Entry<IConstraint, IMessage>> getAllowedErrors() {
        return allowedErrors;
    }

}
