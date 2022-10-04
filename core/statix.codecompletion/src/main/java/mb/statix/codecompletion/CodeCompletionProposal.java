package mb.statix.codecompletion;

import mb.nabl2.terms.ITerm;

/**
 * A code completion proposal.
 */
public final class CodeCompletionProposal {
    private final CCSolverState state;
    private final ITerm term;

    /**
     * Initializes a new instance of the {@link CodeCompletionProposal} class.
     *
     * @param state the state of the proposal
     * @param term the term of the proposal
     */
    public CodeCompletionProposal(CCSolverState state, ITerm term) {
        this.state = state;
        this.term = term;
    }

    /**
     * Gets the state of the proposal.
     *
     * @return the state of the proposal
     */
    public CCSolverState getState() {
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
