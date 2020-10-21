package mb.spoofax.intellij.menu;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import mb.common.util.ListView;
import mb.common.util.StringUtil;
import mb.spoofax.core.language.command.CommandRequest;
import mb.spoofax.core.language.menu.*;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.List;
import java.util.Optional;


/**
 * Builds a menu for a language.
 */
public final class LanguageMenuBuilder {

    private final Provider<LanguageActionGroup> languageActionGroupProvider;
    private final EditorContextLanguageAction.Factory editorContextLanguageActionFactory;

    /**
     * Initializes a new instance of the {@link LanguageMenuBuilder} class.
     */
    @Inject
    public LanguageMenuBuilder(
        Provider<LanguageActionGroup> languageActionGroupProvider,
        EditorContextLanguageAction.Factory editorContextLanguageActionFactory
    ) {
        this.languageActionGroupProvider = languageActionGroupProvider;
        this.editorContextLanguageActionFactory = editorContextLanguageActionFactory;
    }

    /**
     * Builds an IntelliJ action group from the specified list of menu items.
     *
     * @param menuItems the list of menu items
     * @return the built action group
     */
    public DefaultActionGroup build(List<MenuItem> menuItems) {
        final LanguageActionGroup group = this.languageActionGroupProvider.get();
        buildAll(group, menuItems);
        return group;
    }

    /**
     * Builds all menu items in the given iterable, and adds them to the specified group.
     *
     * @param group the action group to add the menu items to
     * @param menuItems the menu items to build
     */
    private void buildAll(DefaultActionGroup group, Iterable<MenuItem> menuItems) {
        for (MenuItem item : menuItems) {
            build(group, item);
        }
    }

    /**
     * Builds a menu item, and adds it to the specified group.
     *
     * @param group the action group to add the menu item to
     * @param menuItem the menu item to build
     */
    private void build(DefaultActionGroup group, MenuItem menuItem) {
        menuItem.caseOf()
            .commandAction(commandAction -> {
                group.add(createCommand(commandAction));
                return Optional.empty();
            })
            .menu((displayName, description, items) -> {
                group.add(createMenu(displayName, description, items));
                return Optional.empty();
            })
            .separator(displayName -> {
                group.add(createSeparator(displayName));
                return Optional.empty();
            });
    }
    /**
     * Creates a menu menu item.
     *
     * @param displayName the display name of the menu item
     * @param description the description of the menu item
     * @param menuItems the items in the menu item
     * @return the created IntelliJ menu
     */
    private DefaultActionGroup createMenu(String displayName, String description, ListView<MenuItem> menuItems) {
        DefaultActionGroup subGroup = new DefaultActionGroup(displayName, true);
        if (!StringUtil.isBlank(description)) {
            Presentation presentation = subGroup.getTemplatePresentation();
            presentation.setDescription(description);
        }
        buildAll(subGroup, menuItems);
        return subGroup;
    }

    /**
     * Creates a menu command item.
     *
     * @param command the menu command item template
     * @return the created IntelliJ action
     */
    private AnAction createCommand(CommandAction command) {
        @SuppressWarnings("rawtypes") CommandRequest commandRequest = command.commandRequest();
        // FIXME: Ensure ID is unique, as multiple commands may use the same command request.
        String id = commandRequest.def().getId();
        return editorContextLanguageActionFactory.create(
            id, commandRequest,
            command.displayName(), command.description(), null);
    }

    /**
     * Creates a menu separator item.
     *
     * @param displayName the separator display name; or an empty string
     * @return the created IntelliJ separator
     */
    private com.intellij.openapi.actionSystem.Separator createSeparator(String displayName) {
        return com.intellij.openapi.actionSystem.Separator.create(displayName);
    }

}
