package mb.tiger.eclipse;

import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.eclipse.editor.SpoofaxEditor;

public class TigerEditor extends SpoofaxEditor {
    @Override protected LanguageComponent getLanguageComponent() {
        return TigerPlugin.getComponent();
    }
}
