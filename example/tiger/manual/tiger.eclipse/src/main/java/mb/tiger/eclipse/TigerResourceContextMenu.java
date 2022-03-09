package mb.tiger.eclipse;

import mb.spoofax.eclipse.menu.ResourceContextMenu;
import mb.tiger.spoofax.TigerParticipant;

public class TigerResourceContextMenu extends ResourceContextMenu {
    public TigerResourceContextMenu() {
        super(TigerEclipseParticipantFactory.getParticipant().getComponent());
    }
}
