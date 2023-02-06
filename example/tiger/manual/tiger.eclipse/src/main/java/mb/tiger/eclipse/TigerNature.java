package mb.tiger.eclipse;

import mb.spoofax.eclipse.nature.SpoofaxNature;
import mb.tiger.spoofax.TigerParticipant;

public class TigerNature extends SpoofaxNature {
    public static final String id = TigerPlugin.pluginId + ".nature";

    public TigerNature() {
        super(TigerEclipseParticipantFactory.getParticipant().getComponent());
    }
}
