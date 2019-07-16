package mb.spoofax.core.language.menu;

public interface MenuItem {
    String getDisplayName();

    void accept(MenuItemVisitor visitor);
}
