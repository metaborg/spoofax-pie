package mb.spoofax.compiler.util;

import mb.spoofax.compiler.adapter.data.MenuItemRepr;
import mb.spoofax.compiler.adapter.data.MenuRepr;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class MenuItemCollection {
    private ArrayList<MenuItemRepr> mainMenuItems;
    private ArrayList<MenuItemRepr> resourceContextMenuItems;
    private ArrayList<MenuItemRepr> editorContextMenuItems;

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


    public void deduplicateMenus() {
        this.mainMenuItems = deduplicateMenus(this.mainMenuItems);
        this.resourceContextMenuItems = deduplicateMenus(this.resourceContextMenuItems);
        this.editorContextMenuItems = deduplicateMenus(this.editorContextMenuItems);
    }

    private ArrayList<MenuItemRepr> deduplicateMenus(List<MenuItemRepr> menuItems) {
        final HashMap<String, MenuRepr> nameToMenu = new HashMap<>();
        final ArrayList<AtomicReference<@Nullable MenuItemRepr>> intermediateMenuItems = new ArrayList<>(menuItems.size());
        int intermediateSize = 0;
        for(final MenuItemRepr menuItem : menuItems) {
            if(menuItem.getMenu().isPresent()) {
                final MenuRepr menu = menuItem.getMenu().get();
                final String name = menu.displayName();
                final @Nullable MenuRepr existingMenu = nameToMenu.get(name);
                if(existingMenu != null) {
                    final MenuRepr mergedMenu = MenuRepr.builder().from(existingMenu).addAllItems(menu.items()).build();
                    nameToMenu.put(name, mergedMenu);
                    intermediateMenuItems.add(new AtomicReference<>(null));
                } else {
                    nameToMenu.put(name, menu);
                    intermediateMenuItems.add(new AtomicReference<>(menuItem));
                    ++intermediateSize;
                }
            } else {
                intermediateMenuItems.add(new AtomicReference<>(menuItem));
                ++intermediateSize;
            }
        }

        final ArrayList<MenuItemRepr> finalMenuItems = new ArrayList<>(intermediateSize);
        for(AtomicReference<@Nullable MenuItemRepr> menuItemRef : intermediateMenuItems) {
            final @Nullable MenuItemRepr menuItem = menuItemRef.get();
            if(menuItem != null) {
                if(menuItem.getMenu().isPresent()) {
                    final MenuRepr menu = menuItem.getMenu().get();
                    final String name = menu.displayName();
                    final MenuRepr actualMenu = nameToMenu.get(name);
                    // Deduplicate the items inside the menu. The `items` method replaces the items from `actualMenu`.
                    final MenuRepr finalMenu = MenuRepr.builder().from(actualMenu).items(deduplicateMenus(actualMenu.items())).build();
                    finalMenuItems.add(MenuItemRepr.menu(finalMenu));
                } else {
                    finalMenuItems.add(menuItem);
                }
            }
        }
        return finalMenuItems;
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
