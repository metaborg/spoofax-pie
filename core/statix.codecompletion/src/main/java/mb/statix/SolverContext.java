package mb.statix;

import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.ITermVar;
import mb.nabl2.terms.stratego.StrategoTerms;
import mb.statix.constraints.messages.IMessage;
import mb.statix.sequences.InterruptiblePredicate;
import mb.statix.solver.IConstraint;
import mb.statix.spec.Spec;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;

/**
 * The context in which the search is performed.
 */
public final class SolverContext {

    private final StrategoTerms strategoTerms;
    @Nullable private final ITermVar focusVar;
    private final Collection<Map.Entry<IConstraint, IMessage>> allowedErrors;
    private final Predicate<ITerm> isInjPredicate;

    /**
     * Initializes a new instance of the {@link SolverContext} class.
     * @param focusVar the focus variable; or {@code null}
     * @param strategoTerms the stratego terms
     */
    public SolverContext(
        StrategoTerms strategoTerms,
        @Nullable ITermVar focusVar,
        Collection<Map.Entry<IConstraint, IMessage>> allowedErrors,
        Predicate<ITerm> isInjPredicate
    ) {
        this.strategoTerms = strategoTerms;
        this.focusVar = focusVar;
        this.allowedErrors = allowedErrors;
        this.isInjPredicate = isInjPredicate;
    }

    /**
     * The {@link StrategoTerms} object.
     *
     * @return the {@link StrategoTerms} object
     */
    public StrategoTerms getStrategoTerms() { return this.strategoTerms; }

    /**
     * The focus variable.
     *
     * This is used for debugging.
     *
     * @return the focus variable; or {@code null}
     */
    @Nullable public ITermVar getFocusVar() {
        return this.focusVar;
    }

    /**
     * Creates a copy of this {@link SolverContext} with the specified focus variable.
     *
     * @param focusVar the focus variable; or {@code null}
     * @return the modified copy of the {@link SolverContext}
     */
    public SolverContext withFocusVar(@Nullable ITermVar focusVar) {
        return new SolverContext(strategoTerms, focusVar, allowedErrors, isInjPredicate);
    }

    /**
     * The allowed errors.
     *
     * This is used to know which errors existed in the project before code completion was invoked.
     *
     * @return a collection of constraints/message pairs that are allowed
     */
    public Collection<Map.Entry<IConstraint, IMessage>> getAllowedErrors() {
        return allowedErrors;
    }

    /**
     * Creates a copy of this {@link SolverContext} with the specified allowed errors.
     *
     * @param allowedErrors a collection of constraints/message pairs that are allowed
     * @return the modified copy of the {@link SolverContext}
     */
    public SolverContext withAllowedErrors(Collection<Map.Entry<IConstraint, IMessage>> allowedErrors) {
        return new SolverContext( strategoTerms, focusVar, allowedErrors, isInjPredicate);
    }

    /**
     * The is-injection predicate.
     *
     * @return the predicate
     */
    public Predicate<ITerm> getIsInjPredicate() {
        return isInjPredicate;
    }
}
