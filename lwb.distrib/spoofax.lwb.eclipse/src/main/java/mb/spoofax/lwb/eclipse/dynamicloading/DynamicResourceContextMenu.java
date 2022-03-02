package mb.spoofax.lwb.eclipse.dynamicloading;

import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.eclipse.EclipseLanguageComponent;
import mb.spoofax.eclipse.menu.MenuShared;
import mb.spoofax.lwb.eclipse.SpoofaxLwbPlugin;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.jface.action.IContributionItem;

import java.util.ArrayList;
import java.util.Collections;

public class DynamicResourceContextMenu extends MenuShared {
    @Override public IContributionItem[] getContributionItems() {
        final ArrayList<IContributionItem> items = new ArrayList<>();
        SpoofaxLwbPlugin.getDynamicLoadingComponent().getDynamicComponentManager().getDynamicComponents().forEach(component -> {
            final @Nullable LanguageComponent languageComponent = component.getLanguageComponent().get();
            if(languageComponent instanceof EclipseLanguageComponent) {
                final MenuShared menu = ((EclipseLanguageComponent)languageComponent).getResourceContextMenu();
                menu.initialize(serviceLocator);
                Collections.addAll(items, menu.getContributionItems());
            }
        });
        return items.toArray(new IContributionItem[0]);
    }
}
