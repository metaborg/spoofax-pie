package mb.statix;

import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.ITermVar;
import mb.statix.constraints.messages.IMessage;
import mb.statix.solver.IConstraint;
import mb.tego.strategies.Strategy;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.Map;

/**
 * The context in which the search is performed.
 */
public final class SolverContext {

    private final @Nullable ITermVar focusVar;
    private final Collection<Map.Entry<IConstraint, IMessage>> allowedErrors;
    private final Strategy<ITerm, @Nullable ITerm> isInjPredicate;
    private final boolean completeDeterministic;

    /**
     * Initializes a new instance of the {@link SolverContext} class.
     */
    public SolverContext(
        @Nullable ITermVar focusVar,
        Collection<Map.Entry<IConstraint, IMessage>> allowedErrors,
        Strategy<ITerm, @Nullable ITerm> isInjPredicate,
        boolean completeDeterministic
    ) {
        this.focusVar = focusVar;
        this.allowedErrors = allowedErrors;
        this.isInjPredicate = isInjPredicate;
        this.completeDeterministic = completeDeterministic;
    }

    /**
     * The focus variable.
     *
     * This is used for debugging.
     *
     * @return the focus variable; or {@code null}
     */
    public @Nullable ITermVar getFocusVar() {
        return this.focusVar;
    }

    /**
     * Creates a copy of this {@link SolverContext} with the specified focus variable.
     *
     * @param focusVar the focus variable; or {@code null}
     * @return the modified copy of the {@link SolverContext}
     */
    public SolverContext withFocusVar(@Nullable ITermVar focusVar) {
        return new SolverContext(focusVar, allowedErrors, isInjPredicate, completeDeterministic);
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
        return new SolverContext(focusVar, allowedErrors, isInjPredicate, completeDeterministic);
    }

    /**
     * The is-injection predicate.
     *
     * @return the predicate
     */
    public Strategy<ITerm, @Nullable ITerm> getIsInjPredicate() {
        return isInjPredicate;
    }

    /**
     * Whether to perform deterministic completion.
     *
     * @return {@code true} to perform deterministic completion; otherwise, {@code false}
     */
    public boolean isCompleteDeterministic() {
        return completeDeterministic;
    }

}
