package mb.spoofax.eclipse.menu;

import mb.common.region.Selection;
import mb.common.util.ListView;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.LanguageInstance;
import mb.spoofax.core.language.command.CommandContext;
import mb.spoofax.core.language.command.CommandExecutionType;
import mb.spoofax.core.language.command.CommandRequest;
import mb.spoofax.core.language.command.ResourcePathWithKinds;
import mb.spoofax.core.language.menu.CommandAction;
import mb.spoofax.core.language.menu.MenuItem;
import mb.spoofax.eclipse.EclipseIdentifiers;
import mb.spoofax.eclipse.EclipseLanguageComponent;
import mb.spoofax.eclipse.SpoofaxEclipseComponent;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.spoofax.eclipse.editor.SpoofaxEditor;
import mb.spoofax.eclipse.resource.EclipseDocumentResource;
import mb.spoofax.eclipse.resource.EclipseResource;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import java.util.Optional;

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
        final Optional<Selection> selection = editor.getSelection();
        final CommandContext context = new CommandContext(filePath != null ? ResourcePathWithKinds.file(filePath) : null, documentKey, selection.orElse(null));

        final LanguageInstance languageInstance = languageComponent.getLanguageInstance();
        final EclipseIdentifiers identifiers = languageComponent.getEclipseIdentifiers();
        final MenuManager langMenu = new MenuManager(languageInstance.getDisplayName());

        final String transformCommandId = identifiers.getTransformCommand();
        for(MenuItem menuItem : getMenuItems(languageInstance)) {
            menuItem.accept(new EclipseMenuItemVisitor(langMenu) {
                @Override
                protected void transformAction(IContributionManager menu, CommandAction command) {
                    CommandRequest commandRequest = command.getCommandRequest();
                    if(commandRequest.executionType == CommandExecutionType.AutomaticContinuous) {
                        return; // Automatic continuous execution is not supported when manually invoking commands.
                    }
                    if(!context.isSupportedBy(commandRequest.def.getRequiredContextTypes())) {
                        return; // Context is not supported by command.
                    }
                    menu.add(createCommand(transformCommandId, commandRequest, context, command.getDisplayName(), command.getDescription()));
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
