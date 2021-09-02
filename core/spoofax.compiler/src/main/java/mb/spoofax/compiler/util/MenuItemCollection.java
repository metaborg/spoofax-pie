package mb.spoofax.compiler.util;

import mb.spoofax.compiler.adapter.data.MenuItemRepr;

import java.util.ArrayList;
import java.util.Collection;

public class MenuItemCollection {
    private final ArrayList<MenuItemRepr> mainMenuItems;
    private final ArrayList<MenuItemRepr> resourceContextMenuItems;
    private final ArrayList<MenuItemRepr> editorContextMenuItems;

    public MenuItemCollection(
        Collection<MenuItemRepr> mainMenuItems,
        Collection<MenuItemRepr> resourceContextMenuItems,
        Collection<MenuItemRepr> editorContextMenuItems
    ) {
        this.mainMenuItems = new ArrayList<>(mainMenuItems);
        this.resourceContextMenuItems = new ArrayList<>(resourceContextMenuItems);
        this.editorContextMenuItems = new ArrayList<>(editorContextMenuItems);
    }


    public void addMainMenuItem(MenuItemRepr menuItem) {
        this.mainMenuItems.add(menuItem);
    }

    public void addResourceContextMenuItem(MenuItemRepr menuItem) {
        this.resourceContextMenuItems.add(menuItem);
    }

    public void addEditorContextMenuItem(MenuItemRepr menuItem) {
        this.editorContextMenuItems.add(menuItem);
    }


    public ArrayList<MenuItemRepr> getMainMenuItems() {
        return mainMenuItems;
    }

    public ArrayList<MenuItemRepr> getResourceContextMenuItems() {
        return resourceContextMenuItems;
    }

    public ArrayList<MenuItemRepr> getEditorContextMenuItems() {
        return editorContextMenuItems;
    }
}
