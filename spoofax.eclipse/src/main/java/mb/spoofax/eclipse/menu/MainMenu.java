package mb.spoofax.eclipse.menu;

import mb.spoofax.eclipse.EclipseLanguageComponent;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;

public class MainMenu extends AbstractMenu {
    private final EclipseLanguageComponent languageComponent;


    public MainMenu(EclipseLanguageComponent languageComponent) {
        this.languageComponent = languageComponent;
    }


    @Override protected IContributionItem[] getContributionItems() {
        return new IContributionItem[0];
    }
}
