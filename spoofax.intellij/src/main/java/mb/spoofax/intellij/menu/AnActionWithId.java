package mb.spoofax.intellij.menu;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.swing.*;


/**
 * An action with an associated ID.
 */
public abstract class AnActionWithId extends AnAction {

    private final String id;

    /**
     * Initializes a new instance of the {@link AnActionWithId} class.
     *
     * @param id          The ID of the action.
     * @param text        The text of the action; or <code>null</code>.
     * @param description The description of the action; or <code>null</code>.
     * @param icon        The icon of the action; or <code>null</code>.
     */
    protected AnActionWithId(
            final String id,
            @Nullable final String text,
            @Nullable final String description,
            @Nullable final Icon icon) {
        super(text, description, icon);
        this.id = id;
    }
    /**
     * Gets the ID of the action.
     *
     * @return The ID of the action.
     */
    public String getId() {
        return this.id;
    }

    @Override
    public abstract void actionPerformed(final AnActionEvent anActionEvent);
}