package mb.spoofax.intellij.pie;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import mb.pie.api.Pie;
import mb.spoofax.intellij.SpoofaxIntellijComponent;
import mb.spoofax.intellij.SpoofaxPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * Drops the PIE store.
 */
public final class DropStoreAction extends AnAction {

    private final Pie pie;

    public DropStoreAction() {
        final SpoofaxIntellijComponent component = SpoofaxPlugin.getComponent();
        this.pie = component.getPie();
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        pie.dropStore();
    }

}
