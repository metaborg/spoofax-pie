package mb.spoofax.eclipse.menu;

import mb.common.util.ListView;
import mb.spoofax.core.language.menu.MenuItem;
import mb.spoofax.core.language.menu.MenuItemVisitor;
import mb.spoofax.core.language.transform.TransformRequest;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;

import java.util.Stack;

public abstract class EclipseMenuItemVisitor implements MenuItemVisitor {
    private final Stack<MenuManager> menuStack = new Stack<>();


    public EclipseMenuItemVisitor(MenuManager rootMenu) {
        menuStack.push(rootMenu);
    }


    protected abstract void transformAction(MenuManager menu, String displayName, TransformRequest transformRequest);


    @Override public void menuPush(String displayName, ListView<MenuItem> items) {
        final MenuManager menuManager = new MenuManager(displayName);
        menuStack.peek().add(menuManager);
        menuStack.push(menuManager);
    }

    @Override public void menuPop() {
        menuStack.pop();
    }


    @Override public void transformAction(String displayName, TransformRequest transformRequest) {
        transformAction(menuStack.peek(), displayName, transformRequest);
    }

    @Override public void separator() {
        menuStack.peek().add(new Separator());
    }
}
