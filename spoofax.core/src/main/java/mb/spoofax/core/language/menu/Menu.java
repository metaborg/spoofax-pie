package mb.spoofax.core.language.menu;

import mb.common.util.ListView;

public class Menu implements MenuItem {
    private final String displayName;
    private final ListView<MenuItem> items;


    public Menu(String displayName, ListView<MenuItem> items) {
        this.displayName = displayName;
        this.items = items;
    }

    public Menu(String displayName, MenuItem... items) {
        this.displayName = displayName;
        this.items = ListView.of(items);
    }


    public ListView<MenuItem> getItems() {
        return items;
    }


    @Override public String getDisplayName() {
        return displayName;
    }

    @Override public void accept(MenuItemVisitor visitor) {
        visitor.menuPush(displayName, items);
        for(MenuItem item : items) {
            item.accept(visitor);
        }
        visitor.menuPop();
    }
}
