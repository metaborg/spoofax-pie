package mb.spoofax.eclipse.editor;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.*;

public abstract class EditorTrackerBase implements IWindowListener, IPartListener2 {
    public void register() {
        final IWorkbench workbench = PlatformUI.getWorkbench();
        Display.getDefault().asyncExec(() -> {
            final IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
            for(IWorkbenchWindow window : windows) {
                windowOpened(window);
            }
            workbench.addWindowListener(this);
        });
    }

    public void unregister() {
        final IWorkbench workbench = PlatformUI.getWorkbench();
        workbench.removeWindowListener(this);
    }


    // IWindowListener implementation

    @Override public void windowOpened(IWorkbenchWindow window) {
        window.getPartService().addPartListener(this);
    }

    @Override public void windowClosed(IWorkbenchWindow window) {
        window.getPartService().removePartListener(this);
    }


    // IPartListener2 implementation

    @Override public void partBroughtToTop(IWorkbenchPartReference partRef) {

    }

    @Override public void partOpened(IWorkbenchPartReference partRef) {

    }

    @Override public void partHidden(IWorkbenchPartReference partRef) {

    }

    @Override public void partVisible(IWorkbenchPartReference partRef) {

    }

    @Override public void partInputChanged(IWorkbenchPartReference partRef) {

    }
}
