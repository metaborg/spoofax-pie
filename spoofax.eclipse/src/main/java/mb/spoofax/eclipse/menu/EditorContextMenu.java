package mb.spoofax.eclipse.menu;

import mb.common.region.Region;
import mb.common.util.EnumSetView;
import mb.common.util.ListView;
import mb.spoofax.core.language.LanguageInstance;
import mb.spoofax.core.language.menu.MenuItem;
import mb.spoofax.core.language.transform.*;
import mb.spoofax.eclipse.EclipseIdentifiers;
import mb.spoofax.eclipse.EclipseLanguageComponent;
import mb.spoofax.eclipse.editor.SpoofaxEditor;
import mb.spoofax.eclipse.resource.EclipseResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

public class EditorContextMenu extends MenuShared {
    private final EclipseLanguageComponent languageComponent;


    public EditorContextMenu(EclipseLanguageComponent languageComponent) {
        this.languageComponent = languageComponent;
    }


    @Override protected IContributionItem[] getContributionItems() {
        final @Nullable IWorkbenchPart activePart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().getActivePart();
        final SpoofaxEditor editor;
        if(activePart instanceof SpoofaxEditor) {
            editor = (SpoofaxEditor) activePart;
        } else {
            // Not a context menu for a Spoofax editor.
            return new IContributionItem[0];
        }
        if(languageComponent != editor.getLanguageComponent() /* Reference equality intended */) {
            // Context menu for a Spoofax editor of a different language.
            return new IContributionItem[0];
        }
        final @Nullable IFile file = editor.getFile();
        final @Nullable Region selectedRegion = editor.getSelectedRegion();

        final LanguageInstance languageInstance = languageComponent.getLanguageInstance();
        final EclipseIdentifiers identifiers = languageComponent.getEclipseIdentifiers();
        final MenuManager langMenu = new MenuManager(languageInstance.getDisplayName());

        final String transformCommandId = identifiers.getTransformCommand();
        for(MenuItem menuItem : languageInstance.getResourceContextMenuItems()) {
            menuItem.accept(new EclipseMenuItemVisitor(langMenu) {
                @Override
                protected void transformAction(MenuManager menu, String displayName, TransformRequest transformRequest) {
                    final EnumSetView<TransformSubjectType> supportedTypes = transformRequest.transformDef.getSupportedSubjectTypes();
                    final ListView<TransformInput> inputs;
                    if(file != null && selectedRegion != null && supportedTypes.contains(TransformSubjectType.FileRegion)) {
                        inputs = ListView.of(new TransformInput(new FileRegionSubject(new EclipseResourcePath(file), selectedRegion)));
                    } else if(file != null && supportedTypes.contains(TransformSubjectType.File)) {
                        inputs = ListView.of(new TransformInput(new FileSubject(new EclipseResourcePath(file))));
                    } else if(supportedTypes.contains(TransformSubjectType.None)) {
                        inputs = ListView.of(new TransformInput(new NoneSubject()));
                    } else {
                        return;
                    }
                    menu.add(transformCommand(transformCommandId, transformRequest, inputs, displayName));
                }
            });
        }

        return new IContributionItem[]{langMenu};
    }
}
