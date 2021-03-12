package mb.spoofax.lwb.eclipse;

import mb.spoofax.eclipse.menu.MenuShared;
import mb.spoofax.eclipse.util.SelectionUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.menus.IWorkbenchContribution;

import java.util.ArrayList;

public class ResourceContextMenu extends MenuShared implements IWorkbenchContribution {
    @Override protected IContributionItem[] getContributionItems() {
        final @Nullable ISelection simpleSelection = SelectionUtil.getActiveSelection();
        if(!(simpleSelection instanceof IStructuredSelection)) {
            return new IContributionItem[0];
        }
        final IStructuredSelection selection = (IStructuredSelection)simpleSelection;
        final ArrayList<IProject> eclipseProjects = SelectionUtil.toProjects(selection);
        if(eclipseProjects.isEmpty()) return new IContributionItem[0];
        final MenuManager menu = new MenuManager("Spoofax LWB");
        boolean addNature = false;
        boolean removeNature = false;
        for(IProject project : eclipseProjects) {
            try {
                if(!SpoofaxLwbNature.hasNature(project)) {
                    addNature = true;
                } else {
                    removeNature = true;
                }
            } catch(CoreException e) {
                // Ignore
            }
        }
        if(addNature) {
            menu.add(createCommand(SpoofaxLwbNature.AddHandler.commandId));
        }
        if(removeNature) {
            menu.add(createCommand(SpoofaxLwbNature.RemoveHandler.commandId));
        }
        return new IContributionItem[]{menu};
    }
}
