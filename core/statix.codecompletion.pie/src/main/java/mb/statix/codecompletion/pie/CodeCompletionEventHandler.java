package mb.statix.codecompletion.pie;

/**
 * Code completion event handler.
 */
public interface CodeCompletionEventHandler {

    /** Begins the code completion task. */
    void begin();

    /** Begins the parsing. */
    void beginParse();
    /** Ends the parsing. */
    void endParse();

    /** Beings the preparation. */
    void beginPreparation();
    /** Ends the preparation. */
    void endPreparation();

    /** Begins the analysis. */
    void beginAnalysis();
    /** Ends the analysis. */
    void endAnalysis();

    /** Begins the code completion strategy. */
    void beginCodeCompletion();
    /** Ends the code completion strategy. */
    void endCodeCompletion();

    /** Beings the finishing. */
    void beginFinishing();
    /** Ends the finishing. */
    void endFinishing();

    /** Ends the code completion task. */
    void end();

}

