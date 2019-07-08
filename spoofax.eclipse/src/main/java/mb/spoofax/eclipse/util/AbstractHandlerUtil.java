package mb.spoofax.eclipse.util;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Utility functions for {@link AbstractHandler}.
 */
public final class AbstractHandlerUtil {
    /**
     * Converts selection in given execution event into a structured selection.
     *
     * @param event Execution event.
     * @return Structured selection, or null if selection in given execution event is not a structured selection.
     */
    public static @Nullable IStructuredSelection toStructured(ExecutionEvent event) {
        final @Nullable ISelection selection = HandlerUtil.getCurrentSelection(event);
        if(selection == null) {
            return null;
        }
        return SelectionUtil.toStructured(selection);
    }


    /**
     * Retrieves all resources from selection in given execution event.
     *
     * @param event Execution event.
     * @return Selected resources, or null if no structured selection could be found.
     */
    public static @Nullable Iterable<IResource> toResources(ExecutionEvent event) {
        final @Nullable IStructuredSelection selection = toStructured(event);
        if(selection == null) {
            return null;
        }
        return SelectionUtil.toResources(selection);
    }

    /**
     * Retrieves all files from selection in given execution event.
     *
     * @param event Execution event.
     * @return Selected files, or null if no structed selection could be found.
     */
    public static Iterable<IFile> toFiles(ExecutionEvent event) {
        final @Nullable IStructuredSelection selection = toStructured(event);
        if(selection == null) {
            return null;
        }
        return SelectionUtil.toFiles(selection);
    }

    /**
     * Attempts to retrieve the project from the selection in given execution event.
     *
     * @param event Execution event.
     * @return Selected project, or null if it could not be retrieved.
     */
    public static @Nullable IProject toProject(ExecutionEvent event) {
        final @Nullable IStructuredSelection selection = toStructured(event);
        if(selection == null) {
            return null;
        }
        return SelectionUtil.toProject(selection);
    }
}
