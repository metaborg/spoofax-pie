package mb.spoofax.eclipse.editor;

import mb.log.api.Logger;
import mb.spoofax.eclipse.EclipseIdentifiers;
import mb.spoofax.eclipse.SpoofaxEclipseComponent;
import mb.spoofax.eclipse.SpoofaxPlugin;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.*;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;

public class EditorRegistry implements IWindowListener, IPartListener2 {
    private final Logger logger;

    private final EclipseIdentifiers eclipseIdentifiers;

    private IContextService contextService;

    private @Nullable IContextActivation contextActivation = null;
    private @Nullable SpoofaxEditor currentActive = null;
    private @Nullable SpoofaxEditor previousActive = null;


    public EditorRegistry(EclipseIdentifiers eclipseIdentifiers) {
        final SpoofaxEclipseComponent component = SpoofaxPlugin.getComponent();
        this.logger = component.getLoggerFactory().create(getClass());

        this.eclipseIdentifiers = eclipseIdentifiers;
    }


    public void register() {
        final IWorkbench workbench = PlatformUI.getWorkbench();
        // COMPAT: DO NOT REMOVE CAST, it is required for older versions of Eclipse.
        @SuppressWarnings("RedundantCast") final IContextService contextService = (IContextService) workbench.getService(IContextService.class);
        this.contextService = contextService;

        Display.getDefault().asyncExec(() -> {
            final IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
            for(IWorkbenchWindow window : windows) {
                windowOpened(window);
            }
            final @Nullable IWorkbenchWindow activeWindow = workbench.getActiveWorkbenchWindow();
            if(activeWindow != null) {
                final @Nullable IWorkbenchPage activePage = activeWindow.getActivePage();
                if(activePage != null) {
                    final @Nullable IEditorPart activeEditorPart = activePage.getActiveEditor();
                    if(activeEditorPart != null) {
                        final @Nullable SpoofaxEditor editor = getEditor(activeEditorPart);
                        if(editor != null) {
                            activated(editor);
                        }
                    }
                }
            }
            workbench.addWindowListener(EditorRegistry.this);
        });
    }


    // IWindowListener implementation

    @Override public void windowActivated(IWorkbenchWindow window) {
        final @Nullable SpoofaxEditor editor = getEditor(window.getPartService().getActivePart());
        if(editor == null) {
            return;
        }
        activated(editor);
    }

    @Override public void windowDeactivated(IWorkbenchWindow window) {
        final @Nullable SpoofaxEditor editor = getEditor(window.getPartService().getActivePart());
        if(editor == null) {
            return;
        }
        deactivated(editor);
    }

    @Override public void windowOpened(IWorkbenchWindow window) {
        window.getPartService().addPartListener(this);
    }

    @Override public void windowClosed(IWorkbenchWindow window) {
        window.getPartService().removePartListener(this);
    }


    // IPartListener2 implementation

    @Override public void partActivated(IWorkbenchPartReference partRef) {
        final @Nullable SpoofaxEditor editor = getEditor(partRef);
        if(editor != null) {
            activated(editor);
        } else if(partRef instanceof IEditorReference) {
            activatedOtherEditor();
        }
    }

    @Override public void partBroughtToTop(IWorkbenchPartReference partRef) {

    }

    @Override public void partClosed(IWorkbenchPartReference partRef) {
        final @Nullable SpoofaxEditor editor = getEditor(partRef);
        if(editor != null) {
            removed(editor);
        }
    }

    @Override public void partDeactivated(IWorkbenchPartReference partRef) {
        final @Nullable SpoofaxEditor editor = getEditor(partRef);
        if(editor != null) {
            deactivated(editor);
        }
    }

    @Override public void partOpened(IWorkbenchPartReference partRef) {

    }

    @Override public void partHidden(IWorkbenchPartReference partRef) {

    }

    @Override public void partVisible(IWorkbenchPartReference partRef) {

    }

    @Override public void partInputChanged(IWorkbenchPartReference partRef) {

    }


    // Event helper methods.

    private void removed(SpoofaxEditor editor) {
        logger.trace("Removing {}", editor);
        if(currentActive == editor) {
            logger.trace("Unsetting active (by remove) {}", editor);
            unsetCurrent();
        }
        if(previousActive == editor) {
            logger.trace("Unsetting latest (by remove) {}", editor);
            previousActive = null;
        }
    }

    private void activated(SpoofaxEditor editor) {
        logger.trace("Setting active {}", editor);
        setCurrent(editor);
        logger.trace("Setting latest {}", editor);
        previousActive = editor;
    }

    private void activatedOtherEditor() {
        logger.trace("Unsetting active (by activate other) {}", currentActive);
        unsetCurrent();
        logger.trace("Unsetting latest (by activate other) {}", previousActive);
        previousActive = null;
    }

    private void deactivated(SpoofaxEditor editor) {
        if(currentActive == editor) {
            logger.trace("Unsetting active (by deactivate) {}", currentActive);
            unsetCurrent();
        }
    }


    // Setting/unsetting the current editor and context.

    private void setCurrent(SpoofaxEditor editor) {
        currentActive = editor;
        if(contextActivation == null) {
            contextActivation = contextService.activateContext(eclipseIdentifiers.getContext());
        }
    }

    private void unsetCurrent() {
        currentActive = null;
        if(contextActivation != null) {
            contextService.deactivateContext(contextActivation);
            contextActivation = null;
        }
    }


    // Helper methods for casting to SpoofaxEditor instances.

    private static @Nullable SpoofaxEditor getEditor(IWorkbenchPartReference part) {
        return getEditor(part.getPart(false));
    }

    private static @Nullable SpoofaxEditor getEditor(IWorkbenchPart part) {
        if(part instanceof SpoofaxEditor) {
            return (SpoofaxEditor) part;
        }
        return null;
    }
}
