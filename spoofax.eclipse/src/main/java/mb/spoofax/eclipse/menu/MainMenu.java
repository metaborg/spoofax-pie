package mb.spoofax.eclipse.menu;

import mb.spoofax.eclipse.EclipseLanguageComponent;
import org.eclipse.jface.action.IContributionItem;

public class MainMenu extends MenuShared {
    private final EclipseLanguageComponent languageComponent;


    public MainMenu(EclipseLanguageComponent languageComponent) {
        this.languageComponent = languageComponent;
    }


    @Override protected IContributionItem[] getContributionItems() {
        return new IContributionItem[0];
    }
}
