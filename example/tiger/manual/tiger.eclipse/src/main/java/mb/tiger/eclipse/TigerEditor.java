package mb.tiger.eclipse;

import mb.spoofax.eclipse.editor.SpoofaxEditor;

public class TigerEditor extends SpoofaxEditor {
    public TigerEditor() {
        super(TigerLanguage.getInstance().getComponent());
    }
}
