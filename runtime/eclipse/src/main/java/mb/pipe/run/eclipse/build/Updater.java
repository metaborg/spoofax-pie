package mb.pipe.run.eclipse.build;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.widgets.Display;

import com.google.inject.Inject;

import mb.pipe.run.core.model.message.Msg;
import mb.pipe.run.core.model.style.Styling;
import mb.pipe.run.eclipse.editor.PipeEditor;
import mb.pipe.run.eclipse.util.MarkerUtils;
import mb.pipe.run.eclipse.util.Nullable;
import mb.pipe.run.eclipse.util.StatusUtils;
import mb.pipe.run.eclipse.util.StyleUtils;
import mb.pipe.run.eclipse.vfs.EclipsePathSrv;
import mb.vfs.path.PPath;

public class Updater {
    private final EclipsePathSrv pathSrv;
    private final StyleUtils styleUtils;


    @Inject public Updater(EclipsePathSrv pathSrv, StyleUtils styleUtils) {
        this.pathSrv = pathSrv;
        this.styleUtils = styleUtils;
    }


    public void updateMessagesAsync(PPath file, Iterable<Msg> msgs) {
        final IResource eclipseFile = pathSrv.unresolve(file);
        updateMessagesAsync(eclipseFile, msgs);
    }

    public void updateMessagesAsync(IResource file, Iterable<Msg> msgs) {
        final WorkspaceJob job = new WorkspaceJob("Update file") {
            @Override public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
                if(monitor != null && monitor.isCanceled())
                    return StatusUtils.cancel();
                MarkerUtils.clearAll(file);
                for(Msg msg : msgs) {
                    MarkerUtils.createMarker(file, msg);
                }
                return StatusUtils.success();
            }
        };
        job.setRule(file);
        job.schedule();
    }


    public void clearMessagesAsync(PPath file) {
        final IResource eclipseFile = pathSrv.unresolve(file);
        clearMessagesAsync(eclipseFile);
    }

    public void clearMessagesAsync(IResource file) {
        updateMessagesAsync(file, new ArrayList<>());
    }


    public void updateMessagesSync(PPath file, Collection<Msg> msgs, @Nullable IProgressMonitor monitor)
        throws CoreException {
        final IResource eclipseFile = pathSrv.unresolve(file);
        updateMessagesSync(eclipseFile, msgs, monitor);
    }

    public void updateMessagesSync(IResource file, Collection<Msg> msgs, @Nullable IProgressMonitor monitor)
        throws CoreException {
        final IWorkspaceRunnable parseMarkerUpdater = new IWorkspaceRunnable() {
            @Override public void run(IProgressMonitor workspaceMonitor) throws CoreException {
                if(workspaceMonitor.isCanceled())
                    return;
                MarkerUtils.clearAll(file);
                for(Msg msg : msgs) {
                    MarkerUtils.createMarker(file, msg);
                }
            }
        };
        ResourcesPlugin.getWorkspace().run(parseMarkerUpdater, file, IWorkspace.AVOID_UPDATE, monitor);
    }


    public void clearMessagesSync(PPath file, @Nullable IProgressMonitor monitor) throws CoreException {
        final IResource eclipseFile = pathSrv.unresolve(file);
        clearMessagesSync(eclipseFile, monitor);
    }

    public void clearMessagesSync(IResource file, @Nullable IProgressMonitor monitor) throws CoreException {
        updateMessagesSync(file, new ArrayList<>(), monitor);
    }


    public void updateStyleAsync(PipeEditor editor, String text, Styling styling, @Nullable IProgressMonitor monitor) {
        final TextPresentation textPresentation = styleUtils.createTextPresentation(styling, text.length());
        updatePresentationAsync(editor, textPresentation, text, monitor);
    }

    public void removeStyleAsync(PipeEditor editor, int textLength, @Nullable IProgressMonitor monitor) {
        final TextPresentation textPresentation = styleUtils.createTextPresentation(Color.black, textLength);
        updatePresentationAsync(editor, textPresentation, null, monitor);
    }

    private void updatePresentationAsync(PipeEditor editor, TextPresentation textPresentation, @Nullable String text,
        @Nullable IProgressMonitor monitor) {
        final ISourceViewer sourceViewer = editor.sourceViewer();
        // Update styling on the main thread, required by Eclipse.
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                if(monitor != null && monitor.isCanceled())
                    return;
                if(text != null && !text.equals(editor.text()))
                    return;
                sourceViewer.changeTextPresentation(textPresentation, true);
            }
        });
    }
}
