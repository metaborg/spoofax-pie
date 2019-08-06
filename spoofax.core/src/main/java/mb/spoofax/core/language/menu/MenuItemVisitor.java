package mb.spoofax.core.language.menu;

import mb.common.util.ListView;
import mb.spoofax.core.language.command.CommandRequest;

public interface MenuItemVisitor {
    void menuPush(String displayName, ListView<MenuItem> items);

    void menuPop();

    void command(String displayName, CommandRequest commandRequest);

    void separator();
}
