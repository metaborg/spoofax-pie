package mb.spoofax.core.language.menu;

/**
 * A menu separator item.
 */
public final class Separator implements MenuItem {

    private final String displayName;

    /**
     * Initializes a new instance of the {@link Separator} class.
     * <p>
     * Some editors, such as IntelliJ, support separators to have names.
     *
     * @param displayName the display name of the separator
     */
    public Separator(String displayName) {
        this.displayName = displayName;
    }

    @Override public String getDisplayName() {
        return this.displayName;
    }

    @Override public String getDescription() {
        return "";
    }

    @Override public void accept(MenuItemVisitor visitor) {
        visitor.separator(this);
    }

}
