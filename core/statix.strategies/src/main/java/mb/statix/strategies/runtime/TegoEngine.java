package mb.statix.strategies.runtime;

import mb.statix.strategies.StrategyDecl;

/**
 * The Tego engine.
 *
 * The methods in this interface are called by strategy implementations.
 */
public interface TegoEngine extends TegoRuntime {

    /**
     * Whether logging is enabled for the given strategy.
     *
     * @param strategy the strategy logging the message
     * @return {@code true} when logging is enabled;
     * otherwise, {@code false}
     */
    boolean isLogEnabled(StrategyDecl strategy);

    /**
     * Logs a debug message.
     *
     * @param strategy the strategy logging the message
     * @param message the message to log
     */
    void log(StrategyDecl strategy, String message);

    /**
     * Logs a debug message.
     *
     * @param strategy the strategy logging the message
     * @param message the message to log
     * @param cause the cause of the message
     */
    void log(StrategyDecl strategy, String message, Throwable cause);

    /**
     * Logs a debug message.
     *
     * @param strategy the strategy logging the message
     * @param message the message to log
     * @param args the message arguments
     */
    void log(StrategyDecl strategy, String message, Object... args);

    /**
     * Logs a debug message.
     *
     * @param strategy the strategy logging the message
     * @param message the message to log
     * @param cause the cause of the message
     * @param args the message arguments
     */
    void log(StrategyDecl strategy, String message, Throwable cause, Object... args);

}
