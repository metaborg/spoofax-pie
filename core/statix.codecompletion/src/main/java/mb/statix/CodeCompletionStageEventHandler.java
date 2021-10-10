package mb.statix;

/**
 * Code completion stage event handler.
 */
public interface CodeCompletionStageEventHandler {
    void beginExpandPredicate();
    void endExpandPredicate(int proposalCount);

    void beginExpandInjections();
    void endExpandInjections(int proposalCount);

    void beginExpandQueries();
    void endExpandQueries(int proposalCount);

    void beginExpandDeterministic();
    void endExpandDeterministic(int proposalCount);
}
