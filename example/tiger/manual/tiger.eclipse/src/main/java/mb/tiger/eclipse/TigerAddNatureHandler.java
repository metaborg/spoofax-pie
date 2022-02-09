package mb.tiger.eclipse;

import mb.spoofax.eclipse.nature.AddNatureHandler;
import mb.tiger.spoofax.TigerParticipant;

public class TigerAddNatureHandler extends AddNatureHandler {
    public TigerAddNatureHandler() {
        super(TigerEclipseParticipantFactory.getParticipant().getComponent());
    }
}
