package mb.tiger.eclipse;

import mb.spoofax.eclipse.build.SpoofaxProjectBuilder;
import mb.tiger.spoofax.TigerParticipant;

public class TigerProjectBuilder extends SpoofaxProjectBuilder {
    public static final String id = TigerPlugin.pluginId + ".builder";

    public TigerProjectBuilder() {
        super(TigerEclipseParticipantFactory.getParticipant().getComponent(), TigerEclipseParticipantFactory.getParticipant().getPieComponent());
    }
}
