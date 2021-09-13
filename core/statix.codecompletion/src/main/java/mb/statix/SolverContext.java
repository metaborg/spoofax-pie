package mb.statix;

import mb.nabl2.terms.ITermVar;
import mb.nabl2.terms.stratego.StrategoTerms;
import mb.statix.constraints.messages.IMessage;
import mb.statix.solver.IConstraint;
import mb.statix.spec.Spec;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.Map;

/**
 * The context in which the search is performed.
 */
public final class SolverContext {

    private final Spec spec;
    private final StrategoTerms strategoTerms;
    @Nullable private final ITermVar focusVar;
    private final Collection<Map.Entry<IConstraint, IMessage>> allowedErrors;

    /**
     * Initializes a new instance of the {@link SolverContext} class.
     * @param spec the specification
     * @param focusVar the focus variable; or {@code null}
     * @param strategoTerms the stratego terms
     */
    public SolverContext(Spec spec, StrategoTerms strategoTerms, @Nullable ITermVar focusVar, Collection<Map.Entry<IConstraint, IMessage>> allowedErrors) {
        this.spec = spec;
        this.strategoTerms = strategoTerms;
        this.focusVar = focusVar;
        this.allowedErrors = allowedErrors;
    }

    /**
     * The specification.
     *
     * @return the specification
     */
    public Spec getSpec() {
        return this.spec;
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
        return new SolverContext(spec, strategoTerms, focusVar, allowedErrors);
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
        return new SolverContext(spec, strategoTerms, focusVar, allowedErrors);
    }
}
