package mb.spoofax.eclipse.menu;

import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.eclipse.util.SelectionUtil;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;

import java.util.ArrayList;

public abstract class ContextMenu extends CompoundContributionItem implements IWorkbenchContribution {
    private final LanguageComponent languageComponent;
    private final String natureId;
    private final String addNatureCommandId;
    private final String removeNatureCommandId;

    private @MonotonicNonNull IServiceLocator serviceLocator;


    public ContextMenu(LanguageComponent languageComponent, String natureId, String addNatureCommandId, String removeNatureCommandId) {
        this.languageComponent = languageComponent;
        this.natureId = natureId;
        this.addNatureCommandId = addNatureCommandId;
        this.removeNatureCommandId = removeNatureCommandId;
    }

    @Override public void initialize(@NonNull IServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
    }


    @Override protected IContributionItem[] getContributionItems() {
        final @Nullable ISelection simpleSelection = SelectionUtil.getActiveSelection();
        if(!(simpleSelection instanceof IStructuredSelection)) {
            return new IContributionItem[0];
        }
        final IStructuredSelection selection = (IStructuredSelection) simpleSelection;

        final MenuManager langMenu = new MenuManager(languageComponent.getLanguageInstance().getDisplayName());

        final ArrayList<IProject> projects = SelectionUtil.toProjects(selection);
        if(!projects.isEmpty()) {
            boolean addNature = false;
            boolean removeNature = false;
            for(IProject project : projects) {
                try {
                    if(!project.hasNature(natureId)) {
                        addNature = true;
                    } else {
                        removeNature = true;
                    }
                } catch(CoreException e) {
                    // Ignore
                }
            }
            if(addNature) {
                langMenu.add(new CommandContributionItem(
                    new CommandContributionItemParameter(serviceLocator, null, addNatureCommandId,
                        CommandContributionItem.STYLE_PUSH)));
            }
            if(removeNature) {
                langMenu.add(new CommandContributionItem(
                    new CommandContributionItemParameter(serviceLocator, null, removeNatureCommandId,
                        CommandContributionItem.STYLE_PUSH)));
            }
        }

        return new IContributionItem[]{langMenu};
    }
}
