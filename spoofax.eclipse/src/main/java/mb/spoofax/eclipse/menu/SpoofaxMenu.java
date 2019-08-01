package mb.spoofax.eclipse.menu;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;

public class SpoofaxMenu extends CompoundContributionItem implements IWorkbenchContribution {
    @SuppressWarnings("NullableProblems") private @MonotonicNonNull IServiceLocator serviceLocator;

    @Override public void initialize(@NonNull IServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
    }


    @Override protected IContributionItem[] getContributionItems() {
        final MenuManager spoofaxMenu = new MenuManager("Spoofax");
        final MenuManager debugMenu = new MenuManager("Debug");
        spoofaxMenu.add(debugMenu);
        final MenuManager pieMenu = new MenuManager("PIE");
        debugMenu.add(pieMenu);

        return new IContributionItem[]{spoofaxMenu};
    }
}
