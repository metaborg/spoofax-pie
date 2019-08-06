package mb.spoofax.eclipse.menu;

import mb.common.region.Region;
import mb.common.region.Selection;
import mb.common.region.Selections;
import mb.common.util.EnumSetView;
import mb.common.util.ListView;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.LanguageInstance;
import mb.spoofax.core.language.menu.MenuItem;
import mb.spoofax.core.language.transform.*;
import mb.spoofax.eclipse.EclipseIdentifiers;
import mb.spoofax.eclipse.EclipseLanguageComponent;
import mb.spoofax.eclipse.SpoofaxEclipseComponent;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.spoofax.eclipse.editor.SpoofaxEditor;
import mb.spoofax.eclipse.resource.EclipseDocumentResource;
import mb.spoofax.eclipse.resource.EclipseResource;
import mb.spoofax.eclipse.transform.TransformUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import java.util.Optional;

import static mb.spoofax.core.language.transform.TransformExecutionType.ManualContinuous;
import static mb.spoofax.core.language.transform.TransformExecutionType.ManualOnce;

public class EditorContextMenu extends MenuShared {
    private final EclipseLanguageComponent languageComponent;


    public EditorContextMenu(EclipseLanguageComponent languageComponent) {
        final SpoofaxEclipseComponent component = SpoofaxPlugin.getComponent();
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
        final @Nullable EclipseDocumentResource documentResource = editor.getResource();
        final @Nullable ResourceKey documentKey;
        final @Nullable ResourcePath filePath;
        if(documentResource != null) {
            documentKey = documentResource.getKey();
            final @Nullable EclipseResource file = documentResource.getFile();
            if(file != null) {
                filePath = file.getPath();
            } else {
                filePath = null;
            }
        } else {
            documentKey = null;
            filePath = null;
        }
        final Selection selection = editor.getSelection();

        final LanguageInstance languageInstance = languageComponent.getLanguageInstance();
        final EclipseIdentifiers identifiers = languageComponent.getEclipseIdentifiers();
        final MenuManager langMenu = new MenuManager(languageInstance.getDisplayName());

        final String transformCommandId = identifiers.getTransformCommand();
        for(MenuItem menuItem : getMenuItems(languageInstance)) {
            menuItem.accept(new EclipseMenuItemVisitor(langMenu) {
                @Override
                protected void transformAction(IContributionManager menu, String displayName, TransformRequest transformRequest) {
                    final EnumSetView<TransformContextType> supportedTypes = transformRequest.transformDef.getSupportedContextTypes();
                    final TransformExecutionType executionType = transformRequest.executionType;
                    final ListView<TransformContext> contexts;
                    final Optional<Region> region = Selections.getRegion(selection);
                    final Optional<Integer> offset = Selections.getOffset(selection);
                    if(executionType == ManualContinuous) {
                        // Prefer editors.
                        if(documentKey != null && region.isPresent() && supportedTypes.contains(TransformContextType.EditorWithRegion)) {
                            contexts = TransformUtil.context(TransformContexts.editorWithRegion(documentKey, region.get()));
                        } else if(documentKey != null && offset.isPresent() && supportedTypes.contains(TransformContextType.EditorWithOffset)) {
                            contexts = TransformUtil.context(TransformContexts.editorWithOffset(documentKey, offset.get()));
                        } else if(documentKey != null && supportedTypes.contains(TransformContextType.Editor)) {
                            contexts = TransformUtil.context(TransformContexts.editor(documentKey));
                        }
                        // Then files.
                        else if(filePath != null && region.isPresent() && supportedTypes.contains(TransformContextType.FileWithRegion)) {
                            contexts = TransformUtil.context(TransformContexts.fileWithRegion(filePath, region.get()));
                        } else if(filePath != null && offset.isPresent() && supportedTypes.contains(TransformContextType.FileWithOffset)) {
                            contexts = TransformUtil.context(TransformContexts.fileWithOffset(filePath, offset.get()));
                        } else if(filePath != null && supportedTypes.contains(TransformContextType.File)) {
                            contexts = TransformUtil.context(TransformContexts.file(filePath));
                        }
                        // None subject is not supported.
                        else {
                            return;
                        }
                    } else if(executionType == ManualOnce) {
                        // Prefer files.
                        if(filePath != null && region.isPresent() && supportedTypes.contains(TransformContextType.FileWithRegion)) {
                            contexts = TransformUtil.context(TransformContexts.fileWithRegion(filePath, region.get()));
                        } else if(filePath != null && offset.isPresent() && supportedTypes.contains(TransformContextType.FileWithOffset)) {
                            contexts = TransformUtil.context(TransformContexts.fileWithOffset(filePath, offset.get()));
                        } else if(filePath != null && supportedTypes.contains(TransformContextType.File)) {
                            contexts = TransformUtil.context(TransformContexts.file(filePath));
                        }
                        // Then editors.
                        else if(documentKey != null && region.isPresent() && supportedTypes.contains(TransformContextType.EditorWithRegion)) {
                            contexts = TransformUtil.context(TransformContexts.editorWithRegion(documentKey, region.get()));
                        } else if(documentKey != null && offset.isPresent() && supportedTypes.contains(TransformContextType.EditorWithOffset)) {
                            contexts = TransformUtil.context(TransformContexts.editorWithOffset(documentKey, offset.get()));
                        } else if(documentKey != null && supportedTypes.contains(TransformContextType.Editor)) {
                            contexts = TransformUtil.context(TransformContexts.editor(documentKey));
                        }
                        // Last resort: none subject.
                        else if(supportedTypes.contains(TransformContextType.None)) {
                            contexts = TransformUtil.context(TransformContexts.none());
                        } else {
                            return;
                        }
                    } else {
                        // Other execution types are not supported.
                        return;
                    }
                    menu.add(transformCommand(transformCommandId, transformRequest, contexts, displayName));
                }
            });
        }

        if(addLangMenu()) {
            return new IContributionItem[]{langMenu};
        } else {
            return langMenu.getItems();
        }
    }

    protected ListView<MenuItem> getMenuItems(LanguageInstance languageInstance) {
        return languageInstance.getEditorContextMenuItems();
    }

    protected boolean addLangMenu() {
        return true;
    }
}
