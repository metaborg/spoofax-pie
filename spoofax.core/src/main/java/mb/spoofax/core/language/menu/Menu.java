package mb.spoofax.core.language.menu;

import mb.common.util.ListView;

/**
 * A menu menu item.
 */
public final class Menu implements MenuItem {

    private final String displayName;
    private final String description;
    private final ListView<MenuItem> items;

    /**
     * Initializes a new instance of the {@link Menu} class.
     *
     * @param displayName The display name.
     * @param description The short description of the menu.
     * @param items       The items in the menu.
     */
    public Menu(String displayName, String description, ListView<MenuItem> items) {
        this.displayName = displayName;
        this.description = description;
        this.items = items;
    }

    /**
     * Initializes a new instance of the {@link Menu} class.
     *
     * @param displayName The display name.
     * @param description The short description of the menu.
     * @param items       The items in the menu.
     */
    public Menu(String displayName, String description, MenuItem... items) {
        this(displayName, description, ListView.of(items));
    }

    /**
     * Initializes a new instance of the {@link Menu} class.
     *
     * @param displayName The display name.
     * @param items       The items in the menu.
     */
    public Menu(String displayName, ListView<MenuItem> items) {
        this(displayName, "", items);
    }

    /**
     * Initializes a new instance of the {@link Menu} class.
     *
     * @param displayName The display name.
     * @param items       The items in the menu.
     */
    public Menu(String displayName, MenuItem... items) {
        this(displayName, "", ListView.of(items));
    }

    /**
     * Gets the items in the menu.
     *
     * @return A list of items.
     */
    public ListView<MenuItem> getItems() {
        return items;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public void accept(MenuItemVisitor visitor) {
        visitor.menu(this);
    }

}
