package mb.spoofax.eclipse.pie;

import mb.pie.api.MixedSession;
import mb.pie.api.Pie;
import mb.spoofax.eclipse.SpoofaxEclipseComponent;
import mb.spoofax.eclipse.SpoofaxPlugin;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import java.io.IOException;

public class GarbageCollectHandler extends AbstractHandler {
    private final Pie pie;
    private final PieRunner pieRunner;

    public GarbageCollectHandler() {
        final SpoofaxEclipseComponent component = SpoofaxPlugin.getComponent();
        this.pie = component.getPie();
        this.pieRunner = component.getPieRunner();
    }

    @Override public @Nullable Object execute(ExecutionEvent event) throws ExecutionException {
        // POTI: creating a generic (not language-specific) session, which has less task definitions, which may cause
        // problems when running garbage collection.
        try(final MixedSession session = pie.newSession()) {
            pieRunner.deleteUnobservedTasks(session, null);
        } catch(IOException e) {
            throw new ExecutionException("Deleting unobserved tasks (garbage collection) failed unexpectedly", e);
        }
        return null;
    }
}
