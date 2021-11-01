package mb.statix.codecompletion;

import mb.nabl2.terms.ITerm;

/**
 * A code completion proposal.
 */
public final class CodeCompletionProposal {
    private final SolverState state;
    private final ITerm term;

    /**
     * Initializes a new instance of the {@link CodeCompletionProposal} class.
     *
     * @param state the state of the proposal
     * @param term the term of the proposal
     */
    public CodeCompletionProposal(SolverState state, ITerm term) {
        this.state = state;
        this.term = term;
    }

    /**
     * Gets the state of the proposal.
     *
     * @return the state of the proposal
     */
    public SolverState getState() {
        return state;
    }

    /**
     * Gets the term of the proposal.
     *
     * @return the term of the proposal
     */
    public ITerm getTerm() {
        return term;
    }
}
