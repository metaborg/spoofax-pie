package mb.spoofax.lwb.eclipse.dynamicloading;

import com.google.common.collect.Sets;
import mb.spoofax.eclipse.editor.WindowAndPartListener;
import mb.spoofax.lwb.eclipse.SpoofaxLwbScope;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;

import javax.inject.Inject;
import java.util.Set;

@SpoofaxLwbScope
public class DynamicEditorTracker extends WindowAndPartListener {
    public static final String contextId = "spoofax.lwb.eclipse.dynamicloading.context";

    private @MonotonicNonNull IContextService contextService;
    private @Nullable IContextActivation contextActivation = null;
    private Set<DynamicEditor> editors = Sets.newConcurrentHashSet();


    @Inject public DynamicEditorTracker() {}


    public Iterable<DynamicEditor> getEditors() {
        return editors;
    }


    public void register() {
        final IWorkbench workbench = PlatformUI.getWorkbench();

        // COMPAT: DO NOT REMOVE CAST, it is required for older versions of Eclipse.
        @SuppressWarnings("RedundantCast") final IContextService contextService = (IContextService)workbench.getService(IContextService.class);
        this.contextService = contextService;

        super.register();

        Display.getDefault().asyncExec(() -> {
            final IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
            for(IWorkbenchWindow window : windows) {
                for(IWorkbenchPage page : window.getPages()) {
                    for(IEditorReference editorRef : page.getEditorReferences()) {
                        final @Nullable DynamicEditor editor = getDynamicEditor(editorRef);
                        if(editor != null) {
                            editors.add(editor);
                        }
                    }
                }
            }

            final @Nullable IWorkbenchWindow activeWindow = workbench.getActiveWorkbenchWindow();
            if(activeWindow != null) {
                final @Nullable IWorkbenchPage activePage = activeWindow.getActivePage();
                if(activePage != null) {
                    final @Nullable IEditorPart activeEditorPart = activePage.getActiveEditor();
                    if(activeEditorPart != null) {
                        if(isDynamicEditor(activeEditorPart)) {
                            activateContext();
                        }
                    }
                }
            }
        });
    }

    @Override public void unregister() {
        deactivateContext();
        super.unregister();
        this.editors.clear();
        this.contextService = null;
    }


    // IWindowListener implementation

    @Override public void windowActivated(IWorkbenchWindow window) {
        if(isDynamicEditor(window.getPartService().getActivePart())) {
            activateContext();
        }
    }

    @Override public void windowDeactivated(IWorkbenchWindow window) {
        if(isDynamicEditor(window.getPartService().getActivePart())) {
            deactivateContext();
        }
    }


    // IPartListener2 implementation

    @Override public void partActivated(IWorkbenchPartReference partRef) {
        if(isDynamicEditor(partRef)) {
            activateContext();
        }
    }

    @Override public void partDeactivated(IWorkbenchPartReference partRef) {
        if(isDynamicEditor(partRef)) {
            deactivateContext();
        }
    }

    @Override public void partOpened(IWorkbenchPartReference partRef) {
        final @Nullable DynamicEditor editor = getDynamicEditor(partRef);
        if(editor != null) {
            editors.add(editor);
        }
    }

    @Override public void partClosed(IWorkbenchPartReference partRef) {
        final @Nullable DynamicEditor editor = getDynamicEditor(partRef);
        if(editor != null) {
            deactivateContext();
            editors.remove(editor);
        }
    }


    // Event helper methods.

    private void activateContext() {
        if(contextActivation == null) {
            contextActivation = contextService.activateContext(contextId);
        }
    }

    private void deactivateContext() {
        if(contextActivation != null) {
            contextService.deactivateContext(contextActivation);
            contextActivation = null;
        }
    }


    // Editor helper methods

    private boolean isDynamicEditor(IWorkbenchPartReference part) {
        return DynamicEditor.id.equals(part.getId());
    }

    private boolean isDynamicEditor(@Nullable IWorkbenchPart part) {
        return part instanceof DynamicEditor;
    }

    private @Nullable DynamicEditor getDynamicEditor(IWorkbenchPartReference part) {
        return getDynamicEditor(part.getPart(false));
    }

    private @Nullable DynamicEditor getDynamicEditor(IWorkbenchPart part) {
        if(part instanceof DynamicEditor) {
            return (DynamicEditor)part;
        }
        return null;
    }
}
