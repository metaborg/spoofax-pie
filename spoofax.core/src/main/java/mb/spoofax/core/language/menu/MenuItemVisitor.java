package mb.spoofax.core.language.menu;

import mb.common.util.ListView;
import mb.spoofax.core.language.transform.TransformRequest;

public interface MenuItemVisitor {
    void menuPush(String displayName, ListView<MenuItem> items);

    void menuPop();

    void transformAction(String displayName, TransformRequest transformRequest);

    void separator();
}
