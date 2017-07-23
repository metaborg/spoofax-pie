package mb.log;

import javax.annotation.Nullable;

public interface Logger {
    void trace(String msg);

    void trace(String msg, @Nullable Throwable t);

    void trace(String fmt, Object... args);

    void trace(String fmt, Throwable t, Object... args);

    boolean traceEnabled();


    void debug(String msg);

    void debug(String msg, @Nullable Throwable t);

    void debug(String fmt, Object... args);

    void debug(String fmt, Throwable t, Object... args);

    boolean debugEnabled();


    void info(String msg);

    void info(String msg, @Nullable Throwable t);

    void info(String fmt, Object... args);

    void info(String fmt, Throwable t, Object... args);

    boolean infoEnabled();


    void warn(String msg);

    void warn(String msg, @Nullable Throwable t);

    void warn(String fmt, Object... args);

    void warn(String fmt, Throwable t, Object... args);

    boolean warnEnabled();


    void error(String msg);

    void error(String msg, @Nullable Throwable t);

    void error(String fmt, Object... args);

    void error(String fmt, Throwable t, Object... args);

    boolean errorEnabled();


    void log(Level level, String msg);

    void log(Level level, String msg, @Nullable Throwable t);

    void log(Level level, String fmt, Object... args);

    void log(Level level, String fmt, Throwable t, Object... args);

    boolean enabled(Level level);


    Logger forContext(Class<?> clazz);

    Logger forContext(String name);
}
