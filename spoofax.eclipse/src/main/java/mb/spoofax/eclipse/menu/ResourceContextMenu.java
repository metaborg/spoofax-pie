package mb.spoofax.eclipse.menu;

import mb.common.util.EnumSetView;
import mb.common.util.ListView;
import mb.spoofax.core.language.LanguageInstance;
import mb.spoofax.core.language.command.CommandContext;
import mb.spoofax.core.language.command.CommandContextType;
import mb.spoofax.core.language.command.CommandRequest;
import mb.spoofax.core.language.menu.MenuItem;
import mb.spoofax.eclipse.EclipseIdentifiers;
import mb.spoofax.eclipse.EclipseLanguageComponent;
import mb.spoofax.eclipse.SpoofaxEclipseComponent;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.spoofax.eclipse.pie.PieRunner;
import mb.spoofax.eclipse.resource.EclipseResourcePath;
import mb.spoofax.eclipse.util.SelectionUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Collectors;

public abstract class ResourceContextMenu extends MenuShared {
    private final PieRunner pieRunner;

    private final EclipseLanguageComponent languageComponent;


    public ResourceContextMenu(EclipseLanguageComponent languageComponent) {
        final SpoofaxEclipseComponent component = SpoofaxPlugin.getComponent();
        this.pieRunner = component.getPieRunner();

        this.languageComponent = languageComponent;
    }


    @Override protected IContributionItem[] getContributionItems() {
        final @Nullable ISelection simpleSelection = SelectionUtil.getActiveSelection();
        if(!(simpleSelection instanceof IStructuredSelection)) {
            return new IContributionItem[0];
        }
        final IStructuredSelection selection = (IStructuredSelection) simpleSelection;

        final LanguageInstance languageInstance = languageComponent.getLanguageInstance();
        final EclipseIdentifiers identifiers = languageComponent.getEclipseIdentifiers();
        final MenuManager langMenu = new MenuManager(languageInstance.getDisplayName());

        // Selections.
        // OPTO: prevent allocating ArrayLists of unused Eclipse resource objects.
        final ArrayList<IProject> eclipseProjects = SelectionUtil.toProjects(selection);
        final ArrayList<CommandContext> projectContexts = eclipseProjects.stream().map(EclipseResourcePath::new).map(CommandContext::new).collect(Collectors.toCollection(ArrayList::new));
        final boolean hasProjects = !projectContexts.isEmpty();
        final ArrayList<IContainer> eclipseContainers = SelectionUtil.toContainers(selection);
        final ArrayList<CommandContext> directoryContexts = eclipseContainers.stream().map(EclipseResourcePath::new).map(CommandContext::new).collect(Collectors.toCollection(ArrayList::new));
        final boolean hasDirectories = !directoryContexts.isEmpty();
        final ArrayList<IFile> eclipseLangFiles = SelectionUtil.toFiles(selection);
        for(Iterator<IFile> it = eclipseLangFiles.iterator(); it.hasNext(); ) {
            final IFile file = it.next();
            final @Nullable String fileExtension = file.getFileExtension();
            if(fileExtension == null || !languageInstance.getFileExtensions().contains(fileExtension)) {
                it.remove(); // Remove non-language files.
            }
        }
        final ArrayList<EclipseResourcePath> langFiles = eclipseLangFiles.stream().map(EclipseResourcePath::new).collect(Collectors.toCollection(ArrayList::new));
        final ArrayList<CommandContext> langFileContexts = langFiles.stream().map(CommandContext::new).collect(Collectors.toCollection(ArrayList::new));
        final boolean hasFiles = !langFiles.isEmpty();

        // Add/remove nature.
        if(hasProjects) {
            boolean addNature = false;
            boolean removeNature = false;
            for(IProject project : eclipseProjects) {
                try {
                    if(!project.hasNature(identifiers.getNature())) {
                        addNature = true;
                    } else {
                        removeNature = true;
                    }
                } catch(CoreException e) {
                    // Ignore
                }
            }
            if(addNature) {
                langMenu.add(createCommand(identifiers.getAddNatureCommand()));
            }
            if(removeNature) {
                langMenu.add(createCommand(identifiers.getRemoveAddNatureCommand()));
            }
        }

        // Observe/unobserve check tasks.
        if(hasFiles) {
            boolean showObserve = false;
            boolean showUnobserve = false;
            for(EclipseResourcePath file : langFiles) {
                if(!pieRunner.isCheckObserved(languageComponent, file)) {
                    showObserve = true;
                } else {
                    showUnobserve = true;
                }
            }
            if(showObserve || showUnobserve) {
                langMenu.add(new Separator());
                if(showObserve) {
                    langMenu.add(createCommand(identifiers.getObserveCommand()));
                }
                if(showUnobserve) {
                    langMenu.add(createCommand(identifiers.getUnobserveCommand()));
                }
            }
        }

        // Transformations.
        final String transformCommandId = identifiers.getTransformCommand();
        for(MenuItem menuItem : languageInstance.getResourceContextMenuItems()) {
            menuItem.accept(new EclipseMenuItemVisitor(langMenu) {
                @Override
                protected void transformAction(IContributionManager menu, String displayName, CommandRequest<?> commandRequest) {
                    final EnumSetView<CommandContextType> requiredContexts = commandRequest.def.getRequiredContextTypes();
                    final ArrayList<CommandContext> contexts;
                    if(hasProjects && requiredContexts.contains(CommandContextType.Project)) {
                        contexts = projectContexts;
                    } else if(hasDirectories && requiredContexts.contains(CommandContextType.Directory)) {
                        contexts = directoryContexts;
                    } else if(hasFiles && requiredContexts.contains(CommandContextType.File) || requiredContexts.contains(CommandContextType.Resource)) {
                        contexts = langFileContexts;
                    } else {
                        return;
                    }
                    menu.add(createCommand(transformCommandId, commandRequest, new ListView<>(contexts), displayName));
                }
            });
        }

        return new IContributionItem[]{langMenu};
    }
}
