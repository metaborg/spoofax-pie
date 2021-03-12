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
import mb.spoofax.core.language.menu.MenuItem;
import mb.spoofax.eclipse.EclipseIdentifiers;
import mb.spoofax.eclipse.EclipseLanguageComponent;
import mb.spoofax.eclipse.EclipsePlatformComponent;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.spoofax.eclipse.command.EnclosingCommandContextProvider;
import mb.spoofax.eclipse.editor.SpoofaxEditor;
import mb.spoofax.eclipse.resource.EclipseResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import java.util.Optional;

public class EditorContextMenu extends CommandMenuShared {
    private final EnclosingCommandContextProvider enclosingCommandContextProvider;

    private final EclipseLanguageComponent languageComponent;


    public EditorContextMenu(EclipseLanguageComponent languageComponent) {
        final EclipsePlatformComponent component = SpoofaxPlugin.getPlatformComponent();
        enclosingCommandContextProvider = component.getEnclosingCommandContextProvider();
        this.languageComponent = languageComponent;
    }


    @Override protected IContributionItem[] getContributionItems() {
        final @Nullable IWorkbenchPart activePart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().getActivePart();
        final SpoofaxEditor editor;
        if(activePart instanceof SpoofaxEditor) {
            editor = (SpoofaxEditor)activePart;
        } else {
            // Not a context menu for a Spoofax editor.
            return new IContributionItem[0];
        }
        if(languageComponent != editor.getLanguageComponent() /* Reference equality intended */) {
            // Context menu for a Spoofax editor of a different language.
            return new IContributionItem[0];
        }
        final @Nullable IFile eclipseFile = editor.getFile();
        final @Nullable ResourcePath filePath;
        final @Nullable ResourceKey documentKey;
        if(eclipseFile != null) {
            filePath = new EclipseResourcePath(eclipseFile);
            documentKey = filePath;
        } else {
            documentKey = null;
            filePath = null;
        }
        final Optional<Selection> selection = editor.getSelection();
        final CommandContext context = new CommandContext(filePath != null ? ResourcePathWithKinds.file(filePath) : null, documentKey, selection.orElse(null));

        final LanguageInstance languageInstance = languageComponent.getLanguageInstance();
        final EclipseIdentifiers identifiers = languageComponent.getEclipseIdentifiers();
        final MenuManager langMenu = new MenuManager(languageInstance.getDisplayName());

        final String runCommandCommandId = identifiers.getRunCommand();
        for(MenuItem menuItem : getMenuItems(languageInstance)) {
            EclipseMenuItemVisitor.run(langMenu, menuItem, (menu, commandAction) -> {
                CommandRequest commandRequest = commandAction.commandRequest();
                if(commandRequest.executionType() == CommandExecutionType.AutomaticContinuous) {
                    return; // Automatic continuous execution is not supported when manually invoking commands.
                }
                if(!context.supportsAnyEditorFileType(commandAction.requiredEditorFileTypes())) {
                    return; // Command requires a certain type of file, but the context does not have one.
                }
                if(!context.supportsAnyEditorSelectionType(commandAction.requiredEditorSelectionTypes())) {
                    return; // Command requires a certain type of selection, but the context does not have one.
                }
                final @Nullable CommandContext finalContext = enclosingCommandContextProvider.selectRequired(context, commandAction.requiredEnclosingResourceTypes());
                if(finalContext == null) {
                    return; // Command requires a certain type of enclosing context, but context does not have one.
                }
                menu.add(createCommand(runCommandCommandId, commandRequest, finalContext, commandAction.displayName(), commandAction.description()));
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
