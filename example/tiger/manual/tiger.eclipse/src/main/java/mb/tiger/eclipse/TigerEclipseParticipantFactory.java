package mb.tiger.eclipse;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.runtime.IExecutableExtensionFactory;

public class TigerEclipseParticipantFactory implements IExecutableExtensionFactory {
    private static @Nullable TigerEclipseParticipant instance;

    public static TigerEclipseParticipant getParticipant() {
        if(instance == null) {
            instance = new TigerEclipseParticipant();
        }
        return instance;
    }

    @Override public TigerEclipseParticipant create() {
        return getParticipant();
    }
}
