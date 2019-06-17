package mb.spoofax.eclipse.log;

import mb.common.util.StringFormatter;
import mb.common.util.StringUtils;
import mb.log.api.Level;
import mb.log.api.Logger;
import mb.spoofax.eclipse.util.StatusUtil;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.console.MessageConsole;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EclipseLogger implements Logger, AutoCloseable {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");

    private final String name;

    private final Level statusLogLevel;
    private final ILog statusLog;

    private final Level consoleLogLevel;
    private final PrintWriter consoleWriter;


    public EclipseLogger(String name, Level statusLogLevel, ILog statusLog, Level consoleLogLevel, MessageConsole console) {
        this.name = name;
        this.statusLogLevel = statusLogLevel;
        this.statusLog = statusLog;

        this.consoleLogLevel = consoleLogLevel;
        this.consoleWriter = new PrintWriter(console.newMessageStream());
    }

    @Override public void close() {
        this.consoleWriter.flush();
        this.consoleWriter.close();
    }


    private String format(String format, Object... args) {
        return StringFormatter.format(format, args);
    }

    private void statusLog(IStatus status) {
        statusLog.log(status);
    }

    private String encodeConsoleMessage(String msg, Level level) {
        return dateFormat.format(new Date()) +
            " | " +
            rightPad(level.toString().toUpperCase(), 5) +
            " | " +
            rightPad(Thread.currentThread().getName(), 50) +
            " | " +
            rightPad(name, 50) +
            " | " +
            msg;
    }

    private String rightPad(String str, int length) {
        final int strLength = str.length();
        if(strLength > length) {
            return str.substring(strLength - length, strLength);
        }
        return StringUtils.rightPad(str, length);
    }

    private void consoleLog(String msg, Level level) {
        consoleWriter.println(encodeConsoleMessage(msg, level));
        consoleWriter.flush();
    }

    private void consoleLog(String msg, Throwable cause, Level level) {
        consoleWriter.println(encodeConsoleMessage(msg, level));
        cause.printStackTrace(consoleWriter);
        consoleWriter.flush();
    }


    private boolean isStatusTraceEnabled() {
        return Level.Trace.compareTo(statusLogLevel) >= 0;
    }

    private boolean isConsoleTraceEnabled() {
        return Level.Trace.compareTo(consoleLogLevel) >= 0;
    }

    private void doTrace(String msg) {
        if(isStatusTraceEnabled()) {
            statusLog(StatusUtil.info(msg));
        }
        if(isConsoleTraceEnabled()) {
            consoleLog(msg, Level.Trace);
        }
    }

    private void doTrace(String msg, Throwable cause) {
        if(isStatusTraceEnabled()) {
            statusLog(StatusUtil.info(msg, cause));
        }
        if(isConsoleTraceEnabled()) {
            consoleLog(msg, cause, Level.Trace);
        }
    }

    @Override public boolean isTraceEnabled() {
        return isStatusTraceEnabled() || isConsoleTraceEnabled();
    }

    @Override public void trace(String msg) {
        doTrace(msg);
    }

    @Override public void trace(String msg, Throwable cause) {
        doTrace(msg, cause);
    }

    @Override public void trace(String format, Object... args) {
        if(!isTraceEnabled()) return;
        final String msg = format(format, args);
        doTrace(msg);
    }

    @Override public void trace(String format, Throwable cause, Object... args) {
        if(!isTraceEnabled()) return;
        final String msg = format(format, args);
        doTrace(msg, cause);
    }


    private boolean isStatusDebugEnabled() {
        return Level.Debug.compareTo(statusLogLevel) >= 0;
    }

    private boolean isConsoleDebugEnabled() {
        return Level.Debug.compareTo(consoleLogLevel) >= 0;
    }

    private void doDebug(String msg) {
        if(isStatusDebugEnabled()) {
            statusLog(StatusUtil.info(msg));
        }
        if(isConsoleDebugEnabled()) {
            consoleLog(msg, Level.Debug);
        }
    }

    private void doDebug(String msg, Throwable cause) {
        if(isStatusDebugEnabled()) {
            statusLog(StatusUtil.info(msg, cause));
        }
        if(isConsoleDebugEnabled()) {
            consoleLog(msg, cause, Level.Debug);
        }
    }

    @Override public boolean isDebugEnabled() {
        return isStatusDebugEnabled() || isConsoleDebugEnabled();
    }

    @Override public void debug(String msg) {
        doDebug(msg);
    }

    @Override public void debug(String msg, Throwable cause) {
        doDebug(msg, cause);
    }

    @Override public void debug(String format, Object... args) {
        if(!isDebugEnabled()) return;
        final String msg = format(format, args);
        doDebug(msg);
    }

    @Override public void debug(String format, Throwable cause, Object... args) {
        if(!isDebugEnabled()) return;
        final String msg = format(format, args);
        doDebug(msg, cause);
    }


    private boolean isStatusInfoEnabled() {
        return Level.Info.compareTo(statusLogLevel) >= 0;
    }

    private boolean isConsoleInfoEnabled() {
        return Level.Info.compareTo(consoleLogLevel) >= 0;
    }

    private void doInfo(String msg) {
        if(isStatusInfoEnabled()) {
            statusLog(StatusUtil.info(msg));
        }
        if(isConsoleInfoEnabled()) {
            consoleLog(msg, Level.Info);
        }
    }

    private void doInfo(String msg, Throwable cause) {
        if(isStatusInfoEnabled()) {
            statusLog(StatusUtil.info(msg, cause));
        }
        if(isConsoleInfoEnabled()) {
            consoleLog(msg, cause, Level.Info);
        }
    }

    @Override public boolean isInfoEnabled() {
        return isStatusInfoEnabled() || isConsoleInfoEnabled();
    }

    @Override public void info(String msg) {
        doInfo(msg);
    }

    @Override public void info(String msg, Throwable cause) {
        doInfo(msg, cause);
    }

    @Override public void info(String format, Object... args) {
        if(!isInfoEnabled()) return;
        final String msg = format(format, args);
        doInfo(msg);
    }

    @Override public void info(String format, Throwable cause, Object... args) {
        if(!isInfoEnabled()) return;
        final String msg = format(format, args);
        doInfo(msg, cause);
    }


    private boolean isStatusWarnEnabled() {
        return Level.Warn.compareTo(statusLogLevel) >= 0;
    }

    private boolean isConsoleWarnEnabled() {
        return Level.Warn.compareTo(consoleLogLevel) >= 0;
    }

    private void doWarn(String msg) {
        if(isStatusWarnEnabled()) {
            statusLog(StatusUtil.warn(msg));
        }
        if(isConsoleWarnEnabled()) {
            consoleLog(msg, Level.Warn);
        }
    }

    private void doWarn(String msg, Throwable cause) {
        if(isStatusWarnEnabled()) {
            statusLog(StatusUtil.warn(msg, cause));
        }
        if(isConsoleWarnEnabled()) {
            consoleLog(msg, cause, Level.Warn);
        }
    }

    @Override public boolean isWarnEnabled() {
        return isStatusWarnEnabled() || isConsoleWarnEnabled();
    }

    @Override public void warn(String msg) {
        doWarn(msg);
    }

    @Override public void warn(String msg, Throwable cause) {
        doWarn(msg, cause);
    }

    @Override public void warn(String format, Object... args) {
        if(!isWarnEnabled()) return;
        final String msg = format(format, args);
        doWarn(msg);
    }

    @Override public void warn(String format, Throwable cause, Object... args) {
        if(!isWarnEnabled()) return;
        final String msg = format(format, args);
        doWarn(msg, cause);
    }


    private boolean isStatusErrorEnabled() {
        return Level.Error.compareTo(statusLogLevel) >= 0;
    }

    private boolean isConsoleErrorEnabled() {
        return Level.Error.compareTo(consoleLogLevel) >= 0;
    }

    private void doError(String msg) {
        if(isStatusErrorEnabled()) {
            statusLog(StatusUtil.error(msg));
        }
        if(isConsoleErrorEnabled()) {
            consoleLog(msg, Level.Error);
        }
    }

    private void doError(String msg, Throwable cause) {
        if(isStatusErrorEnabled()) {
            statusLog(StatusUtil.error(msg, cause));
        }
        if(isConsoleErrorEnabled()) {
            consoleLog(msg, cause, Level.Error);
        }
    }

    @Override public boolean isErrorEnabled() {
        return isStatusErrorEnabled() || isConsoleErrorEnabled();
    }

    @Override public void error(String msg) {
        doError(msg);
    }

    @Override public void error(String msg, Throwable cause) {
        doError(msg, cause);
    }

    @Override public void error(String format, Object... args) {
        if(!isErrorEnabled()) return;
        final String msg = format(format, args);
        doError(msg);
    }

    @Override public void error(String format, Throwable cause, Object... args) {
        if(!isErrorEnabled()) return;
        final String msg = format(format, args);
        doError(msg, cause);
    }
}
