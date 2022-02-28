package mb.spoofax.lwb.eclipse.dynamicloading;

import mb.spoofax.eclipse.EclipseLanguageComponent;
import mb.spoofax.eclipse.menu.MenuShared;

public class DynamicMainMenu extends DynamicEditorContextMenu {
    @Override protected MenuShared getLanguageMenu(EclipseLanguageComponent languageComponent) {
        return languageComponent.getMainMenu();
    }
}
