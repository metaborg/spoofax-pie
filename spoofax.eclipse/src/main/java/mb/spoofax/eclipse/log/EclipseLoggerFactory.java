package mb.spoofax.eclipse.log;

import mb.log.api.Level;
import mb.log.api.LoggerFactory;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.spoofax.eclipse.editor.EditorRegistry;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;

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
        return new EclipseLogger(name, statusLogLevel, SpoofaxPlugin.getPlugin().getLog(), consoleLogLevel,
            getConsole(consoleName));
    }

    @Override public EclipseLogger create(Class<?> clazz) {
        final Level statusLogLevel;
        final Level consoleLogLevel;
        if(EditorRegistry.class.isAssignableFrom(clazz)) { // HACK: disable logging for EditorRegistry.
            statusLogLevel = Level.None;
            consoleLogLevel = Level.None;
        } else {
            statusLogLevel = this.statusLogLevel;
            consoleLogLevel = this.consoleLogLevel;
        }
        return new EclipseLogger(clazz.getName(), statusLogLevel, SpoofaxPlugin.getPlugin().getLog(), consoleLogLevel,
            getConsole(consoleName));
    }


    private MessageConsole getConsole(String name) {
        final ConsolePlugin consolePlugin = ConsolePlugin.getDefault();
        final IConsoleManager consoleManager = consolePlugin.getConsoleManager();
        final IConsole[] existingConsoles = consoleManager.getConsoles();
        for(final IConsole existingConsole : existingConsoles) {
            if(name.equals(existingConsole.getName())) {
                return (MessageConsole) existingConsole;
            }
        }
        final MessageConsole newConsole = new MessageConsole(name, null);
        consoleManager.addConsoles(new IConsole[]{newConsole});
        return newConsole;
    }
}
