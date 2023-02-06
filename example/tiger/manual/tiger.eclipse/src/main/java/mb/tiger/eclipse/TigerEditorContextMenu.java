package mb.tiger.eclipse;

import mb.spoofax.eclipse.menu.EditorContextMenu;
import mb.tiger.spoofax.TigerParticipant;

public class TigerEditorContextMenu extends EditorContextMenu {
    public TigerEditorContextMenu() {
        super(TigerEclipseParticipantFactory.getParticipant().getComponent());
    }
}
