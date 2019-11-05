package mb.spoofax.intellij.menu;

import com.intellij.openapi.actionSystem.Anchor;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.components.ApplicationComponent;
import mb.spoofax.core.language.LanguageInstance;
import mb.spoofax.core.language.menu.MenuItem;
import mb.spoofax.intellij.IntellijLanguageComponent;

import java.util.List;


/**
 * Registers the language main menu.
 */
public abstract class MainMenuComponent implements ApplicationComponent {

    private final IntellijLanguageComponent languageComponent;
    private final LanguageMenuBuilder languageMenuBuilder;

    protected MainMenuComponent(IntellijLanguageComponent languageComponent, LanguageMenuBuilder languageMenuBuilder) {
        this.languageComponent = languageComponent;
        this.languageMenuBuilder = languageMenuBuilder;
    }

    protected List<MenuItem> getMenuItems(LanguageInstance languageInstance) {
        return languageInstance.getMainMenuItems().asUnmodifiable();
    }

    @Override
    public void initComponent() {
        LanguageInstance languageInstance = languageComponent.getLanguageInstance();
        List<MenuItem> menuItems = getMenuItems(languageInstance);
        DefaultActionGroup group = this.languageMenuBuilder.build(menuItems);
//        ActionUtils.addAndRegisterActionGroup(group, IdeActions.GROUP_MAIN_MENU, "ToolsMenu", Anchor.AFTER);
        ActionUtils.addAndRegisterActionGroup(group, IdeActions.GROUP_MAIN_MENU, "spoofax.intellij.menu.main", Anchor.AFTER);
    }

}
