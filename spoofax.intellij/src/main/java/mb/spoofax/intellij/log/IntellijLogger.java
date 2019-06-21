package mb.spoofax.intellij.log;

import mb.common.util.StringFormatter;
import mb.log.api.Logger;


/**
 * Logger for IntelliJ.
 */
public final class IntellijLogger implements Logger, AutoCloseable {

    private final com.intellij.openapi.diagnostic.Logger intellijLogger;

    /**
     * Initializes a new instance of the {@link IntellijLogger} class.
     *
     * @param name The name of the logger.
     */
    public IntellijLogger(String name) {
        this.intellijLogger = com.intellij.openapi.diagnostic.Logger.getInstance(name);
    }

    @Override public void close() {
        /* Nothing to do. */
    }

    @Override
    public boolean isTraceEnabled() {
        return this.intellijLogger.isTraceEnabled();
    }

    @Override
    public void trace(String msg) {
        if (!isTraceEnabled()) return;
        this.intellijLogger.trace(msg);
    }

    @Override
    public void trace(String msg, Throwable cause) {
        if (!isTraceEnabled()) return;
        // IntelliJ won't let us print both a trace message and an exception.
        // We give priority to the message, if any.
        if (msg != null && !msg.isEmpty()) {
            this.intellijLogger.trace(msg);
        } else {
            this.intellijLogger.trace(cause);
        }
    }

    @Override
    public void trace(String format, Object... args) {
        if (!isTraceEnabled()) return;
        this.intellijLogger.trace(format(format, args));
    }

    @Override
    public void trace(String format, Throwable cause, Object... args) {
        if (!isTraceEnabled()) return;
        trace(format(format, args), cause);
    }

    @Override
    public boolean isDebugEnabled() {
        return this.intellijLogger.isDebugEnabled();
    }

    @Override
    public void debug(String msg) {
        if (!isDebugEnabled()) return;
        this.intellijLogger.debug(msg);
    }

    @Override
    public void debug(String msg, Throwable cause) {
        if (!isDebugEnabled()) return;
        this.intellijLogger.debug(msg, cause);
    }

    @Override
    public void debug(String format, Object... args) {
        if (!isDebugEnabled()) return;
        this.intellijLogger.debug(format(format, args));
    }

    @Override
    public void debug(String format, Throwable cause, Object... args) {
        if (!isDebugEnabled()) return;
        this.intellijLogger.debug(format(format, args), cause);
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public void info(String msg) {
        if (!isInfoEnabled()) return;
        this.intellijLogger.info(msg);
    }

    @Override
    public void info(String msg, Throwable cause) {
        if (!isInfoEnabled()) return;
        this.intellijLogger.info(msg, cause);
    }

    @Override
    public void info(String format, Object... args) {
        if (!isInfoEnabled()) return;
        this.intellijLogger.info(format(format, args));
    }

    @Override
    public void info(String format, Throwable cause, Object... args) {
        if (!isInfoEnabled()) return;
        this.intellijLogger.info(format(format, args), cause);
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    @Override
    public void warn(String msg) {
        if (!isWarnEnabled()) return;
        this.intellijLogger.warn(msg);
    }

    @Override
    public void warn(String msg, Throwable cause) {
        if (!isWarnEnabled()) return;
        this.intellijLogger.warn(msg, cause);
    }

    @Override
    public void warn(String format, Object... args) {
        if (!isWarnEnabled()) return;
        this.intellijLogger.warn(format(format, args));
    }

    @Override
    public void warn(String format, Throwable cause, Object... args) {
        if (!isWarnEnabled()) return;
        this.intellijLogger.warn(format(format, args), cause);
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public void error(String msg) {
        if (!isErrorEnabled()) return;
        this.intellijLogger.error(msg);
    }

    @Override
    public void error(String msg, Throwable cause) {
        if (!isErrorEnabled()) return;
        this.intellijLogger.error(msg, cause);
    }

    @Override
    public void error(String format, Object... args) {
        if (!isErrorEnabled()) return;
        this.intellijLogger.error(format(format, args));
    }

    @Override
    public void error(String format, Throwable cause, Object... args) {
        if (!isErrorEnabled()) return;
        this.intellijLogger.error(format(format, args), cause);
    }

    private String format(String format, Object... args) {
        return StringFormatter.format(format, args);
    }

}
