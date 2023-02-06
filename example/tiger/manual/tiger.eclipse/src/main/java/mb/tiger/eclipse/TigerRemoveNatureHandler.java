package mb.tiger.eclipse;

import mb.spoofax.eclipse.nature.RemoveNatureHandler;
import mb.tiger.spoofax.TigerParticipant;

public class TigerRemoveNatureHandler extends RemoveNatureHandler {
    public TigerRemoveNatureHandler() {
        super(TigerEclipseParticipantFactory.getParticipant().getComponent());
    }
}
