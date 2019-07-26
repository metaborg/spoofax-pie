package mb.spoofax.eclipse.menu;

import mb.common.region.Region;
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
import mb.spoofax.eclipse.transform.TransformUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import java.util.Optional;

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
        for(MenuItem menuItem : getMenuItems(languageInstance)) {
            menuItem.accept(new EclipseMenuItemVisitor(langMenu) {
                @Override
                protected void transformAction(MenuManager menu, String displayName, TransformRequest transformRequest) {
                    final EnumSetView<TransformSubjectType> supportedTypes = transformRequest.transformDef.getSupportedSubjectTypes();
                    final ListView<TransformInput> inputs;
                    final Optional<Region> region = Selections.getRegion(selection);
                    final Optional<Integer> offset = Selections.getOffset(selection);
                    if(file != null && region.isPresent() && supportedTypes.contains(TransformSubjectType.FileRegion)) {
                        inputs = TransformUtil.input(TransformSubjects.fileRegion(file, region.get()));
                    } else if(file != null && offset.isPresent() && supportedTypes.contains(TransformSubjectType.FileOffset)) {
                        inputs = TransformUtil.input(TransformSubjects.fileOffset(file, offset.get()));
                    } else if(file != null && supportedTypes.contains(TransformSubjectType.File)) {
                        inputs = TransformUtil.input(TransformSubjects.file(file));
                    } else if(supportedTypes.contains(TransformSubjectType.None)) {
                        inputs = TransformUtil.input(TransformSubjects.none());
                    } else {
                        return;
                    }
                    menu.add(transformCommand(transformCommandId, transformRequest, inputs, displayName));
                }
            });
        }

        return new IContributionItem[]{langMenu};
    }

    protected ListView<MenuItem> getMenuItems(LanguageInstance languageInstance) {
        return languageInstance.getEditorContextMenuItems();
    }
}
