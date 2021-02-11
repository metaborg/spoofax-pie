package mb.tiger.eclipse;

import mb.spoofax.eclipse.menu.EditorContextMenu;

public class TigerEditorContextMenu extends EditorContextMenu {
    public TigerEditorContextMenu() {
        super(TigerLanguage.getInstance().getComponent());
    }
}
