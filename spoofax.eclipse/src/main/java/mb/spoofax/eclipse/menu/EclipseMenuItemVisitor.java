package mb.spoofax.eclipse.menu;

import mb.common.util.ListView;
import mb.spoofax.core.language.menu.MenuItem;
import mb.spoofax.core.language.menu.MenuItemVisitor;
import mb.spoofax.core.language.command.CommandRequest;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;

import java.util.Stack;

public abstract class EclipseMenuItemVisitor implements MenuItemVisitor {
    private final Stack<IContributionManager> menuStack = new Stack<>();


    public EclipseMenuItemVisitor(IContributionManager rootMenu) {
        menuStack.push(rootMenu);
    }


    protected abstract void transformAction(IContributionManager menu, String displayName, CommandRequest commandRequest);


    @Override public void menuPush(String displayName, ListView<MenuItem> items) {
        final MenuManager menuManager = new MenuManager(displayName);
        menuStack.peek().add(menuManager);
        menuStack.push(menuManager);
    }

    @Override public void menuPop() {
        menuStack.pop();
    }


    @Override public void command(String displayName, CommandRequest commandRequest) {
        transformAction(menuStack.peek(), displayName, commandRequest);
    }

    @Override public void separator() {
        menuStack.peek().add(new Separator());
    }
}
