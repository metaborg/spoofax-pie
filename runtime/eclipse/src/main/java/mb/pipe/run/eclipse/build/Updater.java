package mb.pipe.run.eclipse.build;

import java.awt.Color;
import java.util.Collection;

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
import mb.pipe.run.core.path.PPath;
import mb.pipe.run.eclipse.util.MarkerUtils;
import mb.pipe.run.eclipse.util.Nullable;
import mb.pipe.run.eclipse.util.StatusUtils;
import mb.pipe.run.eclipse.util.StyleUtils;
import mb.pipe.run.eclipse.vfs.IEclipseResourceSrv;

public class Updater {
    private final IEclipseResourceSrv resourceSrv;
    private final StyleUtils styleUtils;


    @Inject public Updater(IEclipseResourceSrv resourceSrv, StyleUtils styleUtils) {
        this.resourceSrv = resourceSrv;
        this.styleUtils = styleUtils;
    }


    public void updateMessagesAsync(PPath file, Collection<Msg> msgs) {
        final org.eclipse.core.resources.IResource eclipseFile = resourceSrv.unresolve(file);
        final WorkspaceJob job = new WorkspaceJob("Update file") {
            @Override public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
                if(monitor != null && monitor.isCanceled())
                    return StatusUtils.cancel();
                MarkerUtils.clearAll(eclipseFile);
                for(Msg msg : msgs) {
                    MarkerUtils.createMarker(eclipseFile, msg);
                }
                return StatusUtils.success();
            }
        };
        job.setRule(eclipseFile);
        job.schedule();
    }

    public void updateMessagesSync(PPath file, Collection<Msg> msgs, @Nullable IProgressMonitor monitor)
        throws CoreException {
        final org.eclipse.core.resources.IResource eclipseFile = resourceSrv.unresolve(file);
        updateMessagesSync(eclipseFile, msgs, monitor);
    }

    public void updateMessagesSync(org.eclipse.core.resources.IResource eclipseFile, Collection<Msg> msgs,
        @Nullable IProgressMonitor monitor) throws CoreException {
        final IWorkspaceRunnable parseMarkerUpdater = new IWorkspaceRunnable() {
            @Override public void run(IProgressMonitor workspaceMonitor) throws CoreException {
                if(workspaceMonitor.isCanceled())
                    return;
                MarkerUtils.clearAll(eclipseFile);
                for(Msg msg : msgs) {
                    MarkerUtils.createMarker(eclipseFile, msg);
                }
            }
        };
        ResourcesPlugin.getWorkspace().run(parseMarkerUpdater, eclipseFile, IWorkspace.AVOID_UPDATE, monitor);
    }

    public void updateStyle(ISourceViewer sourceViewer, Styling styling, @Nullable IProgressMonitor monitor) {
        final TextPresentation textPresentation;
        if(styling != null) {
            textPresentation = styleUtils.createTextPresentation(styling);
        } else {
            textPresentation = styleUtils.createTextPresentation(Color.black, 0);
        }

        // Update styling on the main thread, required by Eclipse.
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                if(monitor != null && monitor.isCanceled())
                    return;
                sourceViewer.changeTextPresentation(textPresentation, true);
            }
        });
    }
}
