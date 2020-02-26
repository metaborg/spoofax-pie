package mb.spoofax.eclipse.menu;

import mb.spoofax.core.language.menu.CommandAction;
import mb.spoofax.core.language.menu.MenuItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.MenuManager;

import java.util.Optional;
import java.util.Stack;
import java.util.function.BiConsumer;

public abstract class EclipseMenuItemVisitor {
    private final Stack<IContributionManager> menuStack = new Stack<>();

    public EclipseMenuItemVisitor(IContributionManager rootMenu) {
        menuStack.push(rootMenu);
    }

    public void run(MenuItem menuItem) {
        menuItem.caseOf()
            .commandAction(commandAction -> {
                commandAction(menuStack.peek(), commandAction);
                return Optional.empty();
            })
            .menu((displayName, _description, items) -> {
                final MenuManager menuManager = new MenuManager(displayName);
                menuStack.peek().add(menuManager);
                menuStack.push(menuManager);
                for(MenuItem item : items) {
                    run(item);
                }
                menuStack.pop();
                return Optional.empty();
            })
            .separator(_displayName -> {
                menuStack.peek().add(new org.eclipse.jface.action.Separator());
                return Optional.empty();
            });
    }

    public static void run(IContributionManager rootMenu, MenuItem menuItem, BiConsumer<IContributionManager, CommandAction> commandActionFunc) {
        new EclipseMenuItemVisitor(rootMenu) {
            @Override protected void commandAction(IContributionManager menu, CommandAction commandAction) {
                commandActionFunc.accept(menu, commandAction);
            }
        }.run(menuItem);
    }

    protected abstract void commandAction(IContributionManager menu, CommandAction commandAction);
}
