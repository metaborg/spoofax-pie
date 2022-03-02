package mb.spoofax.lwb.eclipse.dynamicloading;

import mb.spoofax.core.Coordinate;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.eclipse.EclipseLanguageComponent;
import mb.spoofax.eclipse.menu.MenuShared;
import mb.spoofax.lwb.dynamicloading.component.DynamicComponent;
import mb.spoofax.lwb.eclipse.SpoofaxLwbPlugin;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

public class DynamicEditorContextMenu extends MenuShared {
    protected MenuShared getLanguageMenu(EclipseLanguageComponent languageComponent) {
        return languageComponent.getEditorContextMenu();
    }

    @Override public IContributionItem[] getContributionItems() {
        final @Nullable IWorkbenchPart activePart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().getActivePart();
        final DynamicEditor editor;
        if(activePart instanceof DynamicEditor) {
            editor = (DynamicEditor)activePart;
        } else { // Not a dynamic editor.
            return new IContributionItem[0];
        }
        final Coordinate componentCoordinate;
        if(editor.getComponentCoordinate() == null) { // Dynamic editor of unknown language.
            return new IContributionItem[0];
        } else {
            componentCoordinate = editor.getComponentCoordinate();
        }
        final @Nullable DynamicComponent component = SpoofaxLwbPlugin.getDynamicLoadingComponent().getDynamicComponentManager().getDynamicComponent(componentCoordinate).get();
        if(component == null) { // Dynamic editor of unknown language.
            return new IContributionItem[0];
        }
        final @Nullable LanguageComponent languageComponent = component.getLanguageComponent().get();
        if(languageComponent instanceof EclipseLanguageComponent) {
            final MenuShared menu = getLanguageMenu((EclipseLanguageComponent)languageComponent);
            menu.initialize(serviceLocator);
            return menu.getContributionItems();
        }
        return new IContributionItem[0]; // Dynamic component has no language component.
    }
}
