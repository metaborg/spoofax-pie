package mb.spoofax.intellij.log;

import mb.log.api.Level;
import mb.log.api.LoggerFactory;


/**
 * Factory for the IntelliJ logger.
 */
public final class IntellijLoggerFactory implements LoggerFactory {

    @Override public IntellijLogger create(String name) {
        return new IntellijLogger(name);
    }

    @Override public IntellijLogger create(Class<?> clazz) {
        final String loggerName = "#" + clazz.getName();    // This is what IntelliJ uses.
        return new IntellijLogger(loggerName);
    }

}
