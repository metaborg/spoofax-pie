package mb.spoofax.lwb.eclipse.dynamicloading;

import mb.spoofax.eclipse.menu.MenuShared;
import mb.spoofax.lwb.eclipse.SpoofaxLwbParticipant;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

public class DynamicEditorContextMenu extends MenuShared {
    protected MenuShared getLanguageMenu(EclipseDynamicLanguage language) {
        return language.getEditorContextMenu();
    }

    @Override public IContributionItem[] getContributionItems() {
        final @Nullable IWorkbenchPart activePart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().getActivePart();
        final DynamicEditor editor;
        if(activePart instanceof DynamicEditor) {
            editor = (DynamicEditor)activePart;
        } else { // Not a dynamic editor.
            return new IContributionItem[0];
        }
        final String languageId;
        if(editor.getLanguageId() == null) { // Dynamic editor of unknown language.
            return new IContributionItem[0];
        } else {
            languageId = editor.getLanguageId();
        }
        final @Nullable EclipseDynamicLanguage language = (EclipseDynamicLanguage)SpoofaxLwbParticipant.getInstance().getDynamicLoadingComponent().getDynamicComponentManager().getLanguageForId(languageId);
        if(language == null) { // Dynamic editor of unknown language.
            return new IContributionItem[0];
        }

        final MenuShared menu = getLanguageMenu(language);
        menu.initialize(serviceLocator);
        return menu.getContributionItems();
    }
}
