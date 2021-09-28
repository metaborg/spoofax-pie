package mb.tego.strategies.runtime;

import mb.log.api.LoggerFactory;

/**
 * Builds the Tego runtime.
 */
public final class TegoRuntimeBuilder {

    private final LoggerFactory loggerFactory;

    public TegoRuntimeBuilder(
        LoggerFactory loggerFactory
    ) {
        this.loggerFactory = loggerFactory;
    }

    /**
     * Builds the Tego runtime.
     *
     * @return the built Tego runtime
     */
    public TegoRuntime build() {
        return new TegoRuntimeImpl(loggerFactory);
    }
}
