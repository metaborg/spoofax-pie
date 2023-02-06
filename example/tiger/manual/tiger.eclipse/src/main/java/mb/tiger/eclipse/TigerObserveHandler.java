package mb.tiger.eclipse;

import mb.spoofax.eclipse.menu.ObserveHandler;
import mb.tiger.spoofax.TigerParticipant;

public class TigerObserveHandler extends ObserveHandler {
    public TigerObserveHandler() {
        super(TigerEclipseParticipantFactory.getParticipant().getComponent());
    }
}
