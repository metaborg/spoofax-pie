package mb.spoofax.core.language.menu;

import mb.common.util.ListView;
import mb.spoofax.core.language.command.CommandRequest;

/**
 * Visitor interface for {@link MenuItem} objects.
 */
public interface MenuItemVisitor {

    /**
     * Called when visiting a menu item.
     *
     * @param menu The menu item being visited.
     */
    void menu(Menu menu);

    /**
     * Called when visiting a command.
     *
     * @param command The command being visited.
     */
    void command(CommandAction command);

    /**
     * Called when visiting a separator.
     *
     * @param separator The separator being visited.
     */
    void separator(Separator separator);
}
