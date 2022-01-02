package mb.spoofax.lwb.eclipse.dynamicloading;

import mb.spoofax.eclipse.menu.MenuShared;

public class DynamicMainMenu extends DynamicEditorContextMenu {
    @Override protected MenuShared getLanguageMenu(EclipseDynamicLanguage language) {
        return language.getMainMenu();
    }
}
