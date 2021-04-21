package mb.spoofax.lwb.eclipse.dynamicloading;

import mb.spoofax.eclipse.menu.MenuShared;
import mb.spoofax.lwb.dynamicloading.DynamicLanguage;
import mb.spoofax.lwb.eclipse.SpoofaxLwbLifecycleParticipant;
import org.eclipse.jface.action.IContributionItem;

import java.util.ArrayList;
import java.util.Collections;

public class DynamicResourceContextMenu extends MenuShared {
    @Override public IContributionItem[] getContributionItems() {
        final ArrayList<IContributionItem> items = new ArrayList<>();
        for(DynamicLanguage dynamicLanguage : SpoofaxLwbLifecycleParticipant.getInstance().getDynamicLoadingComponent().getDynamicLanguageRegistry().getLanguages()) {
            final MenuShared menu = ((EclipseDynamicLanguage)dynamicLanguage).getResourceContextMenu();
            menu.initialize(serviceLocator);
            Collections.addAll(items, menu.getContributionItems());
        }
        return items.toArray(new IContributionItem[0]);
    }
}
