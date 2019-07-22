package mb.spoofax.eclipse.menu;

import mb.common.region.Selection;
import mb.common.region.Selections;
import mb.common.util.EnumSetView;
import mb.common.util.ListView;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.LanguageInstance;
import mb.spoofax.core.language.menu.MenuItem;
import mb.spoofax.core.language.transform.TransformInput;
import mb.spoofax.core.language.transform.TransformRequest;
import mb.spoofax.core.language.transform.TransformSubjectType;
import mb.spoofax.core.language.transform.TransformSubjects;
import mb.spoofax.eclipse.EclipseIdentifiers;
import mb.spoofax.eclipse.EclipseLanguageComponent;
import mb.spoofax.eclipse.editor.SpoofaxEditor;
import mb.spoofax.eclipse.resource.EclipseResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;
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
        final @Nullable ResourcePath file = editor.getFile().map(EclipseResourcePath::new).orElse(null);
        final Selection selection = editor.getSelection();

        final LanguageInstance languageInstance = languageComponent.getLanguageInstance();
        final EclipseIdentifiers identifiers = languageComponent.getEclipseIdentifiers();
        final MenuManager langMenu = new MenuManager(languageInstance.getDisplayName());

        final String transformCommandId = identifiers.getTransformCommand();
        for(MenuItem menuItem : languageInstance.getResourceContextMenuItems()) {
            menuItem.accept(new EclipseMenuItemVisitor(langMenu) {
                @Override @SuppressWarnings("OptionalGetWithoutIsPresent")
                protected void transformAction(MenuManager menu, String displayName, TransformRequest transformRequest) {
                    final EnumSetView<TransformSubjectType> supportedTypes = transformRequest.transformDef.getSupportedSubjectTypes();
                    final ListView<TransformInput> inputs;
                    if(file != null && selection.isRegion() && supportedTypes.contains(TransformSubjectType.FileRegion)) {
                        inputs = transformInput(TransformSubjects.fileRegion(file, Selections.getRegion(selection).get()));
                    } else if(file != null && selection.isOffset() && supportedTypes.contains(TransformSubjectType.FileOffset)) {
                        inputs = transformInput(TransformSubjects.fileOffset(file, Selections.getOffset(selection).get()));
                    } else if(file != null && supportedTypes.contains(TransformSubjectType.File)) {
                        inputs = transformInput(TransformSubjects.file(file));
                    } else if(supportedTypes.contains(TransformSubjectType.None)) {
                        inputs = transformInput(TransformSubjects.none());
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
