package mb.statix.strategies.runtime;

import mb.log.api.Level;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.log.noop.NoopLogger;
import mb.statix.sequences.Seq;
import mb.statix.strategies.Strategy;
import mb.statix.strategies.Strategy1;
import mb.statix.strategies.Strategy2;
import mb.statix.strategies.Strategy3;
import mb.statix.strategies.StrategyDecl;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Implements the {@link TegoRuntime}.
 */
public final class TegoRuntimeImpl implements TegoRuntime, TegoEngine {

    @Nullable private final LoggerFactory loggerFactory;
    private final Logger log;
    private final Map<StrategyDecl, Logger> loggers = new HashMap<>();
    private final Level strategyLogLevel = Level.Trace;

    /**
     * Initializes a new instance of the {@link TegoRuntimeImpl} class.
     *
     * @param loggerFactory the logger factory
     */
    public TegoRuntimeImpl(
        @Nullable LoggerFactory loggerFactory
    ) {
        this.loggerFactory = loggerFactory;
        this.log = loggerFactory != null ? loggerFactory.create(TegoRuntimeImpl.class) : NoopLogger.instance;
    }

    @Override
    public @Nullable Object eval(StrategyDecl strategy, Object ctx, Object[] args, Object input) {
        enterStrategy(strategy);
        final @Nullable Object result = strategy.evalInternal(this, ctx, args, input);
        return exitStrategy(strategy, result);
    }

    @Override
    public <CTX, T, R> @Nullable R eval(Strategy<CTX, T, R> strategy, CTX ctx, T input) {
        enterStrategy(strategy);
        final @Nullable R result = strategy.evalInternal(this, ctx, input);
        return exitStrategy(strategy, result);
    }

    @Override
    public <CTX, A1, T, R> @Nullable R eval(Strategy1<CTX, A1, T, R> strategy, CTX ctx, A1 arg1, T input) {
        enterStrategy(strategy);
        final @Nullable R result = strategy.evalInternal(this, ctx, arg1, input);
        return exitStrategy(strategy, result);
    }

    @Override
    public <CTX, A1, A2, T, R> @Nullable R eval(Strategy2<CTX, A1, A2, T, R> strategy, CTX ctx, A1 arg1, A2 arg2, T input) {
        enterStrategy(strategy);
        final @Nullable R result = strategy.evalInternal(this, ctx, arg1, arg2, input);
        return exitStrategy(strategy, result);
    }

    @Override
    public <CTX, A1, A2, A3, T, R> @Nullable R eval(Strategy3<CTX, A1, A2, A3, T, R> strategy, CTX ctx, A1 arg1, A2 arg2, A3 arg3, T input) {
        enterStrategy(strategy);
        final @Nullable R result = strategy.evalInternal(this, ctx, arg1, arg2, arg3, input);
        return exitStrategy(strategy, result);
    }

    /**
     * Called just before a strategy is evaluated.
     *
     * @param strategy the strategy that will be evaluated
     */
    private void enterStrategy(StrategyDecl strategy) {
        log.trace("Enter: " + strategy);
    }

    /**
     * Called just after a strategy is evaluated.
     *
     * @param strategy the strategy that was evaluated
     */
    private <R> @Nullable R exitStrategy(StrategyDecl strategy, @Nullable R seq) {
        log.trace("Exit: " + strategy);
        return seq;
    }

    @Override
    public boolean isLogEnabled(StrategyDecl strategy) {
        @Nullable final Logger logger = getLogger(strategy);
        if (logger == null) return false;
        return logger.isEnabled(strategyLogLevel);
    }

    @Override
    public void log(StrategyDecl strategy, String message) {
        @Nullable final Logger logger = getLogger(strategy);
        if (logger == null) return;
        logger.log(strategyLogLevel, message);
    }

    @Override
    public void log(StrategyDecl strategy, String message, Throwable cause) {
        @Nullable final Logger logger = getLogger(strategy);
        if (logger == null) return;
        logger.log(strategyLogLevel, message, cause);
    }

    @Override
    public void log(StrategyDecl strategy, String message, Object... args) {
        @Nullable final Logger logger = getLogger(strategy);
        if (logger == null) return;
        logger.log(strategyLogLevel, message, args);
    }

    @Override
    public void log(StrategyDecl strategy, String message, Throwable cause, Object... args) {
        @Nullable final Logger logger = getLogger(strategy);
        if (logger == null) return;
        logger.log(strategyLogLevel, message, cause);
    }

    /**
     * Gets a logger for the given strategy.
     *
     * @param strategy the strategy to get the logger for
     * @return the logger; or {@code null} if no logger will be available
     */
    @Nullable private Logger getLogger(StrategyDecl strategy) {
        if (loggerFactory == null) return null;
        // Most logger implementations (e.g., Log4J) perform some logger caching
        // using a hashtable lookup on the name. Therefore, we are not going to
        // do any effort here to reuse loggers on every log call.
        return loggerFactory.create(strategy.toString());
    }
}
