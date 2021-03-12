package mb.spoofax.eclipse.menu;

import mb.common.util.ListView;
import mb.spoofax.core.language.LanguageInstance;
import mb.spoofax.core.language.command.CommandContext;
import mb.spoofax.core.language.command.CommandRequest;
import mb.spoofax.core.language.command.HierarchicalResourceType;
import mb.spoofax.core.language.command.ResourcePathWithKind;
import mb.spoofax.core.language.menu.MenuItem;
import mb.spoofax.eclipse.EclipseIdentifiers;
import mb.spoofax.eclipse.EclipseLanguageComponent;
import mb.spoofax.eclipse.EclipsePlatformComponent;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.spoofax.eclipse.command.EnclosingCommandContextProvider;
import mb.spoofax.eclipse.pie.PieRunner;
import mb.spoofax.eclipse.resource.EclipseResourcePath;
import mb.spoofax.eclipse.util.SelectionUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class ResourceContextMenu extends CommandMenuShared {
    private final EnclosingCommandContextProvider enclosingCommandContextProvider;
    private final PieRunner pieRunner;

    private final EclipseLanguageComponent languageComponent;


    public ResourceContextMenu(EclipseLanguageComponent languageComponent) {
        final EclipsePlatformComponent component = SpoofaxPlugin.getPlatformComponent();
        this.enclosingCommandContextProvider = component.getEnclosingCommandContextProvider();
        this.pieRunner = component.getPieRunner();
        this.languageComponent = languageComponent;
    }


    @Override protected IContributionItem[] getContributionItems() {
        final @Nullable ISelection simpleSelection = SelectionUtil.getActiveSelection();
        if(!(simpleSelection instanceof IStructuredSelection)) {
            return new IContributionItem[0];
        }
        final IStructuredSelection selection = (IStructuredSelection)simpleSelection;

        final LanguageInstance languageInstance = languageComponent.getLanguageInstance();
        final EclipseIdentifiers identifiers = languageComponent.getEclipseIdentifiers();
        final MenuManager langMenu = new MenuManager(languageInstance.getDisplayName());

        // Selections.
        // OPTO: prevent allocating ArrayLists of unused Eclipse resource objects.
        final ArrayList<IProject> eclipseProjects = SelectionUtil.toProjects(selection);
        final ArrayList<CommandContext> projectContexts = eclipseProjects.stream()
            .map(project -> ResourcePathWithKind.project(new EclipseResourcePath(project)))
            .map(CommandContext::new)
            .collect(Collectors.toCollection(ArrayList::new));
        final boolean hasProjects = !projectContexts.isEmpty();
        final ArrayList<IContainer> eclipseContainers = SelectionUtil.toContainers(selection);
        final ArrayList<CommandContext> directoryContexts = eclipseContainers.stream()
            .map(directory -> ResourcePathWithKind.directory(new EclipseResourcePath(directory)))
            .map(CommandContext::new)
            .collect(Collectors.toCollection(ArrayList::new));
        final boolean hasDirectories = !directoryContexts.isEmpty();
        final ArrayList<IFile> eclipseLangFiles = SelectionUtil.toFiles(selection);
        for(Iterator<IFile> it = eclipseLangFiles.iterator(); it.hasNext(); ) {
            final IFile file = it.next();
            final @Nullable String fileExtension = file.getFileExtension();
            if(fileExtension == null || !languageInstance.getFileExtensions().contains(fileExtension)) {
                it.remove(); // Remove non-language files.
            }
        }
        final ArrayList<EclipseResourcePath> langFiles = eclipseLangFiles.stream()
            .map(EclipseResourcePath::new)
            .collect(Collectors.toCollection(ArrayList::new));
        final ArrayList<CommandContext> langFileContexts = langFiles.stream()
            .map(ResourcePathWithKind::file)
            .map(CommandContext::new)
            .collect(Collectors.toCollection(ArrayList::new));
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
                langMenu.add(createCommand(identifiers.getRemoveNatureCommand()));
            }
        }

        // Observe/unobserve check tasks.
        if(hasFiles) {
            // TODO: reimplement single/multi-file inspections.
//            boolean showObserve = false;
//            boolean showUnobserve = false;
//            for(EclipseResourcePath file : langFiles) {
//                if(!pieRunner.isCheckObserved(languageComponent, file)) {
//                    showObserve = true;
//                } else {
//                    showUnobserve = true;
//                }
//            }
//            if(showObserve || showUnobserve) {
//                langMenu.add(new Separator());
//                if(showObserve) {
//                    langMenu.add(createCommand(identifiers.getObserveCommand()));
//                }
//                if(showUnobserve) {
//                    langMenu.add(createCommand(identifiers.getUnobserveCommand()));
//                }
//            }
        }

        // Transformations.
        final String runCommandCommandId = identifiers.getRunCommand();
        for(MenuItem menuItem : languageInstance.getResourceContextMenuItems()) {
            EclipseMenuItemVisitor.run(langMenu, menuItem, (menu, commandAction) -> {
                final CommandRequest<?> commandRequest = commandAction.commandRequest();
                final Set<HierarchicalResourceType> requiredResourceType = commandAction.requiredResourceTypes();
                final ArrayList<CommandContext> contexts;
                if(hasProjects && requiredResourceType.contains(HierarchicalResourceType.Project)) {
                    contexts = projectContexts;
                } else if(hasDirectories && requiredResourceType.contains(HierarchicalResourceType.Directory)) {
                    contexts = directoryContexts;
                } else if(hasFiles && requiredResourceType.contains(HierarchicalResourceType.File)) {
                    contexts = langFileContexts;
                } else {
                    return;
                }
                final ArrayList<CommandContext> finalContexts =
                    enclosingCommandContextProvider.filterRequired(contexts.stream(), commandAction.requiredEnclosingResourceTypes())
                        .collect(Collectors.toCollection(ArrayList::new));
                menu.add(createCommand(runCommandCommandId, commandRequest, ListView.of(finalContexts), commandAction.displayName(), commandAction.description()));
            });
        }

        return new IContributionItem[]{langMenu};
    }
}
