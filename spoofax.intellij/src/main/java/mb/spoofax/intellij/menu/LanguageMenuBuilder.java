package mb.spoofax.intellij.menu;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import mb.spoofax.core.language.command.CommandRequest;
import mb.spoofax.core.language.menu.*;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.List;


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
            EditorContextLanguageAction.Factory editorContextLanguageActionFactory) {
        this.languageActionGroupProvider = languageActionGroupProvider;
        this.editorContextLanguageActionFactory = editorContextLanguageActionFactory;
    }

    /**
     * Builds an IntelliJ action group from the specified list of menu items.
     *
     * @param menuItems The list of menu items.
     * @return The built action group.
     */
    public DefaultActionGroup build(List<MenuItem> menuItems) {
        DefaultActionGroup group = this.languageActionGroupProvider.get();
        Visitor visitor = new Visitor(group);
        for (MenuItem menuItem : menuItems) {
            menuItem.accept(visitor);
        }
        return group;
    }


    /**
     * A visitor that adds items to a group.
     */
    private final class Visitor implements MenuItemVisitor {

        private final DefaultActionGroup group;

        /**
         * Initializes a new instance of the {@link Visitor} class.
         *
         * @param group The group to which the visitor adds items.
         */
        public Visitor(DefaultActionGroup group) {
            this.group = group;
        }

        @Override
        public void menu(Menu menu) {
            this.group.add(createMenu(menu));
        }

        @Override
        public void command(CommandAction command) {
            this.group.add(createCommand(command));
        }

        @Override
        public void separator(Separator separator) {
            this.group.add(createSeparator(separator));
        }

        /**
         * Creates a menu menu item.
         *
         * @param menu The menu menu item template.
         * @return The created IntelliJ menu.
         */
        private DefaultActionGroup createMenu(Menu menu) {
            DefaultActionGroup subGroup = new DefaultActionGroup(menu.getDisplayName(), true);
            Visitor subVisitor = new Visitor(subGroup);
            for(MenuItem item : menu.getItems()) {
                item.accept(subVisitor);
            }
            return subGroup;
        }

        /**
         * Creates a menu command item.
         *
         * @param command The menu command item template.
         * @return The created IntelliJ action.
         */
        private AnAction createCommand(CommandAction command) {
            CommandRequest commandRequest = command.getCommandRequest();
            // TODO: Ensure ID is unique!
            // Multiple commands may use the same command request.
            String id = commandRequest.def.getId();
            return editorContextLanguageActionFactory.create(
                    id, commandRequest,
                    command.getDisplayName(), command.getDescription(), null);
        }

        /**
         * Creates a menu separator item.
         *
         * @param separator The menu separator item.
         * @return The created IntelliJ separator.
         */
        private com.intellij.openapi.actionSystem.Separator createSeparator(final Separator separator) {
            return com.intellij.openapi.actionSystem.Separator.create(separator.getDisplayName());
        }
    }

}
