package mb.tiger.eclipse;

import mb.spoofax.eclipse.menu.UnobserveHandler;
import mb.tiger.spoofax.TigerParticipant;

public class TigerUnobserveHandler extends UnobserveHandler {
    public TigerUnobserveHandler() {
        super(TigerEclipseParticipantFactory.getParticipant().getComponent());
    }
}
