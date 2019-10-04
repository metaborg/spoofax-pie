package mb.spoofax.eclipse.menu;

import mb.spoofax.core.language.menu.*;
import mb.spoofax.core.language.command.CommandRequest;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.MenuManager;

import java.util.Stack;

public abstract class EclipseMenuItemVisitor implements MenuItemVisitor {
    private final Stack<IContributionManager> menuStack = new Stack<>();


    public EclipseMenuItemVisitor(IContributionManager rootMenu) {
        menuStack.push(rootMenu);
    }


    protected abstract void transformAction(IContributionManager menu, CommandAction command);

    @Override
    public void menu(Menu menu) {
        final MenuManager menuManager = new MenuManager(menu.getDisplayName());
        menuStack.peek().add(menuManager);
        menuStack.push(menuManager);

        for(MenuItem item : menu.getItems()) {
            item.accept(this);
        }

        menuStack.pop();
    }

    @Override public void command(CommandAction command) {
        transformAction(menuStack.peek(), command);
    }

    @Override public void separator(Separator separator) {
        menuStack.peek().add(new org.eclipse.jface.action.Separator());
    }
}
