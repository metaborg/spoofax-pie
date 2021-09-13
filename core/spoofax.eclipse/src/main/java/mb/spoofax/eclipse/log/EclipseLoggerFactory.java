package mb.spoofax.eclipse.log;

import mb.log.api.Level;
import mb.log.api.LoggerFactory;
import mb.spoofax.eclipse.SpoofaxPlugin;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;

import java.nio.charset.StandardCharsets;

public class EclipseLoggerFactory implements LoggerFactory {
    private final Level statusLogLevel;
    private final Level consoleLogLevel;
    private final String consoleName;


    public EclipseLoggerFactory() {
        this(Level.Warn, Level.Trace, "Spoofax");
    }

    public EclipseLoggerFactory(Level statusLogLevel, Level consoleLogLevel, String consoleName) {
        this.statusLogLevel = statusLogLevel;
        this.consoleLogLevel = consoleLogLevel;
        this.consoleName = consoleName;
    }


    @Override public EclipseLogger create(String name) {
        return new EclipseLogger(name, statusLogLevel, SpoofaxPlugin.getPlugin().getLog(), consoleLogLevel, getConsole(consoleName));
    }

    @Override public EclipseLogger create(Class<?> clazz) {
        return new EclipseLogger(clazz.getName(), statusLogLevel, SpoofaxPlugin.getPlugin().getLog(), consoleLogLevel, getConsole(consoleName));
    }


    private MessageConsole getConsole(String name) {
        final ConsolePlugin consolePlugin = ConsolePlugin.getDefault();
        final IConsoleManager consoleManager = consolePlugin.getConsoleManager();
        final IConsole[] existingConsoles = consoleManager.getConsoles();
        for(final IConsole existingConsole : existingConsoles) {
            if(name.equals(existingConsole.getName())) {
                return (MessageConsole)existingConsole;
            }
        }
        final MessageConsole newConsole = new MessageConsole(name, IConsoleConstants.MESSAGE_CONSOLE_TYPE, null, StandardCharsets.UTF_8.name(), true);
        consoleManager.addConsoles(new IConsole[]{newConsole});
        return newConsole;
    }
}
