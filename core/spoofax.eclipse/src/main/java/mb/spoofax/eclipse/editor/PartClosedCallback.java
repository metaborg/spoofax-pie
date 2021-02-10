package mb.spoofax.eclipse.editor;

import mb.common.util.MultiMap;
import mb.spoofax.core.platform.PlatformScope;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.function.Consumer;

@PlatformScope
public class PartClosedCallback extends EditorTrackerBase {
    private final MultiMap<IWorkbenchPart, Consumer<IWorkbenchPart>> callbacks = MultiMap.withConcurrentHash();

    @Inject public PartClosedCallback() {}

    public void addCallback(IWorkbenchPart part, Consumer<IWorkbenchPart> callback) {
        callbacks.put(part, callback);
    }

    public void removeCallbacksFor(IWorkbenchPart part) {
        callbacks.removeAll(part);
    }


    // IWindowListener implementation

    @Override public void windowActivated(IWorkbenchWindow window) {}

    @Override public void windowDeactivated(IWorkbenchWindow window) {}


    // IPartListener2 implementation

    @Override public void partActivated(IWorkbenchPartReference partRef) {}

    @Override public void partClosed(IWorkbenchPartReference partRef) {
        final @Nullable IWorkbenchPart part = partRef.getPart(false);
        if(part != null) {
            for(Consumer<IWorkbenchPart> callback : callbacks.get(part)) {
                callback.accept(part);
            }
            callbacks.removeAll(part);
        }
    }

    @Override public void partDeactivated(IWorkbenchPartReference partRef) {}
}
