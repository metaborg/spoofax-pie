package mb.spoofax.eclipse.pie;

import mb.pie.api.Pie;
import mb.spoofax.eclipse.SpoofaxEclipseComponent;
import mb.spoofax.eclipse.SpoofaxPlugin;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;

public class DropStoreHandler extends AbstractHandler {
    private final Pie pie;

    public DropStoreHandler() {
        final SpoofaxEclipseComponent component = SpoofaxPlugin.getComponent();
        this.pie = component.getPie();
    }

    @Override public @Nullable Object execute(ExecutionEvent event) {
        pie.dropStore();
        return null;
    }
}
