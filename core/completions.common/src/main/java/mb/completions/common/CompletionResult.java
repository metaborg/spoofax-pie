package mb.completions.common;

import mb.common.util.Experimental;
import mb.common.util.ListView;

import java.io.Serializable;

/**
 * The result of invoking completions.
 */
@Deprecated
public final class CompletionResult implements Serializable {

    private final ListView<CompletionProposal> proposals;
    private final boolean isComplete;

    /**
     * Initializes a new instance of the {@link CompletionResult} class.
     *
     * @param proposals the completion proposals, in the order in which
     *                  they are to be presented to the user
     * @param isComplete whether the list of completions is complete
     */
    public CompletionResult(ListView<CompletionProposal> proposals, @Experimental boolean isComplete) {
        this.proposals = proposals;
        this.isComplete = isComplete;
    }

    /**
     * Gets a list of completion proposals, returned in the order in which
     * they are to be presented to the user.
     *
     * @return a list of completion proposals
     */
    public ListView<CompletionProposal> getProposals() {
        return this.proposals;
    }

    /**
     * Gets whether the list of completions is complete.
     *
     * @return {@code true} when the list is complete;
     * otherwise, {@code false} when narrowing the search (e.g., by typing more characters)
     * may return in new proposals being returned
     */
    @Experimental
    public boolean isComplete() {
        return this.isComplete;
    }
}
