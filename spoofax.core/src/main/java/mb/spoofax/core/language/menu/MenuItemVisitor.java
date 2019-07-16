package mb.spoofax.core.language.menu;

import mb.common.util.ListView;
import mb.spoofax.core.language.transform.TransformRequest;

public interface MenuItemVisitor {
    void menu(String displayName, ListView<MenuItem> items);

    void transformAction(TransformRequest transformRequest);

    void separator();
}
