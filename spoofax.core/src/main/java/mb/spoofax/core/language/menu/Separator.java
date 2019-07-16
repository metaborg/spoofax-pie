package mb.spoofax.core.language.menu;

public class Separator implements MenuItem {
    @Override public String getDisplayName() {
        return "";
    }

    @Override public void accept(MenuItemVisitor visitor) {
        visitor.separator();
    }
}
