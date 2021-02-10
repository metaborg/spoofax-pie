package mb.spoofax.eclipse.editor;

import mb.spoofax.eclipse.EclipseIdentifiers;
import mb.spoofax.eclipse.EclipsePlatformComponent;
import mb.spoofax.eclipse.SpoofaxPlugin;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.*;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;

public class EditorTracker extends EditorTrackerBase {
    private final EclipseIdentifiers eclipseIdentifiers;

    @SuppressWarnings("NullableProblems") private @MonotonicNonNull IContextService contextService;
    private @Nullable IContextActivation contextActivation = null;


    public EditorTracker(EclipseIdentifiers eclipseIdentifiers) {
        final EclipsePlatformComponent component = SpoofaxPlugin.getPlatformComponent();
        this.eclipseIdentifiers = eclipseIdentifiers;
    }

    public void register() {
        final IWorkbench workbench = PlatformUI.getWorkbench();

        // COMPAT: DO NOT REMOVE CAST, it is required for older versions of Eclipse.
        @SuppressWarnings("RedundantCast") final IContextService contextService = (IContextService) workbench.getService(IContextService.class);
        this.contextService = contextService;

        super.register();

        Display.getDefault().asyncExec(() -> {
            final IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
            final @Nullable IWorkbenchWindow activeWindow = workbench.getActiveWorkbenchWindow();
            if(activeWindow != null) {
                final @Nullable IWorkbenchPage activePage = activeWindow.getActivePage();
                if(activePage != null) {
                    final @Nullable IEditorPart activeEditorPart = activePage.getActiveEditor();
                    if(activeEditorPart != null) {
                        if(isSpoofaxEditor(activeEditorPart)) {
                            activateContext();
                        }
                    }
                }
            }
        });
    }


    // IWindowListener implementation

    @Override public void windowActivated(IWorkbenchWindow window) {
        if(isSpoofaxEditor(window.getPartService().getActivePart())) {
            activateContext();
        }
    }

    @Override public void windowDeactivated(IWorkbenchWindow window) {
        if(isSpoofaxEditor(window.getPartService().getActivePart())) {
            deactivateContext();
        }
    }


    // IPartListener2 implementation

    @Override public void partActivated(IWorkbenchPartReference partRef) {
        if(isSpoofaxEditor(partRef)) {
            activateContext();
        }
    }

    @Override public void partClosed(IWorkbenchPartReference partRef) {
        if(isSpoofaxEditor(partRef)) {
            deactivateContext();
        }
    }

    @Override public void partDeactivated(IWorkbenchPartReference partRef) {
        if(isSpoofaxEditor(partRef)) {
            deactivateContext();
        }
    }


    // Event helper methods.

    private void activateContext() {
        if(contextActivation == null) {
            contextActivation = contextService.activateContext(eclipseIdentifiers.getContext());
        }
    }

    private void deactivateContext() {
        if(contextActivation != null) {
            contextService.deactivateContext(contextActivation);
            contextActivation = null;
        }
    }


    // Helper methods for casting to SpoofaxEditor instances.

    private static boolean isSpoofaxEditor(IWorkbenchPartReference part) {
        return isSpoofaxEditor(part.getPart(false));
    }

    private static boolean isSpoofaxEditor(@Nullable IWorkbenchPart part) {
        return part instanceof SpoofaxEditor;
    }
}
