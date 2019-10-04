package mb.spoofax.core.language.menu;

/**
 * A menu item, such as a menu, command, or separator.
 */
public interface MenuItem {

    /**
     * Gets the display name of the menu item.
     *
     * @return The name to display to the user; or an empty string.
     */
    String getDisplayName();

    /**
     * Gets a short description of the menu item.
     *
     * @return A short description to display to the user; or an empty string.
     */
    String getDescription();

    /**
     * Accepts a menu item visitor.
     *
     * @param visitor The visitor.
     */
    void accept(MenuItemVisitor visitor);

}
