package mb.tiger.eclipse;

import mb.spoofax.eclipse.menu.MainMenu;
import mb.tiger.spoofax.TigerParticipant;

public class TigerMainMenu extends MainMenu {
    public TigerMainMenu() {
        super(TigerEclipseParticipantFactory.getParticipant().getComponent());
    }
}
