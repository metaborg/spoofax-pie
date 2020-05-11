package mb.spoofax.eclipse.menu;

import mb.common.util.ListView;
import mb.spoofax.core.language.LanguageInstance;
import mb.spoofax.core.language.menu.MenuItem;
import mb.spoofax.eclipse.EclipseLanguageComponent;

public class MainMenu extends EditorContextMenu /* Same as editor context menu for now */ {
    public MainMenu(EclipseLanguageComponent languageComponent) {
        super(languageComponent);
    }


    @Override protected ListView<MenuItem> getMenuItems(LanguageInstance languageInstance) {
        return languageInstance.getMainMenuItems();
    }

    @Override protected boolean addLangMenu() {
        return false;
    }
}
