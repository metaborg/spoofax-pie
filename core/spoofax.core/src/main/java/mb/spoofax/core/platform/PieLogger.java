package mb.spoofax.core.platform;

import mb.pie.api.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

public class PieLogger implements Logger {
    private final mb.log.api.Logger logger;

    public PieLogger(mb.log.api.Logger logger) {
        this.logger = logger;
    }

    @Override
    public void error(String message, @Nullable Throwable throwable) {
        logger.error(message, throwable);
    }

    @Override
    public void warn(String message, @Nullable Throwable throwable) {
        logger.warn(message, throwable);
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void debug(String message) {
        logger.debug(message);
    }

    @Override
    public void trace(String message) {
        logger.trace(message);
    }
}
