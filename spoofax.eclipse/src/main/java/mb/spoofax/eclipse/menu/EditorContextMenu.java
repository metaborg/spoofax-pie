package mb.spoofax.eclipse.menu;

import mb.spoofax.eclipse.EclipseLanguageComponent;
import org.eclipse.jface.action.IContributionItem;

public class EditorContextMenu extends AbstractMenu {
    private final EclipseLanguageComponent languageComponent;


    public EditorContextMenu(EclipseLanguageComponent languageComponent) {
        this.languageComponent = languageComponent;
    }


    @Override protected IContributionItem[] getContributionItems() {
        return new IContributionItem[0];
    }
}
