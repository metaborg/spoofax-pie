package mb.spoofax.runtime.eclipse.pipeline;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.TextPresentation;

import com.google.inject.Inject;

import mb.log.Logger;
import mb.spoofax.runtime.eclipse.editor.SpoofaxEditor;
import mb.spoofax.runtime.eclipse.util.MarkerUtils;
import mb.spoofax.runtime.eclipse.util.Nullable;
import mb.spoofax.runtime.eclipse.util.StatusUtils;
import mb.spoofax.runtime.eclipse.util.StyleUtils;
import mb.spoofax.runtime.eclipse.vfs.EclipsePathSrv;
import mb.spoofax.runtime.model.message.Msg;
import mb.spoofax.runtime.model.message.PathMsg;
import mb.spoofax.runtime.model.style.Styling;
import mb.vfs.path.PPath;

public class WorkspaceUpdate {
    public static final LockRule lock = new LockRule("Workspace update lock");

    private final Logger logger;
    private final EclipsePathSrv pathSrv;
    private final StyleUtils styleUtils;

    private final ArrayList<PPath> pathsToClear = new ArrayList<>();
    private final ArrayList<PPath> pathsToClearRec = new ArrayList<>();
    private final HashMap<PPath, ArrayList<Msg>> messagesPerPath = new HashMap<>();
    private final ArrayList<StyleUpdate> styleUpdates = new ArrayList<>();


    @Inject public WorkspaceUpdate(Logger logger, EclipsePathSrv pathSrv, StyleUtils styleUtils) {
        this.logger = logger.forContext(getClass());
        this.pathSrv = pathSrv;
        this.styleUtils = styleUtils;
    }


    public void addClear(PPath path) {
        pathsToClear.add(path);
    }

    public void addClearRec(PPath path) {
        pathsToClearRec.add(path);
    }

    public void addMessage(PPath path, Msg msg) {
        ArrayList<Msg> messages = messagesPerPath.get(path);
        if(messages == null) {
            messages = new ArrayList<>();
            messagesPerPath.put(path, messages);
        }
        messages.add(msg);
    }

    public void addMessages(PPath path, Collection<Msg> msgs) {
        ArrayList<Msg> messages = messagesPerPath.get(path);
        if(messages == null) {
            messages = new ArrayList<>();
            messagesPerPath.put(path, messages);
        }
        messages.addAll(msgs);
    }

    public void addMessage(PathMsg msg) {
        addMessage(msg.path(), msg);
    }

    public void addMessageFiltered(PathMsg msg, PPath filter) {
        final PPath path = msg.path();
        if(filter.equals(path)) {
            addMessage(path, msg);
        }
    }

    public void addMessages(Iterable<PathMsg> msgs) {
        for(PathMsg msg : msgs) {
            addMessage(msg);
        }
    }

    public void addMessagesFiltered(Iterable<PathMsg> msgs, PPath filter) {
        for(PathMsg msg : msgs) {
            addMessageFiltered(msg, filter);
        }
    }

    public void replaceMessages(PPath path, ArrayList<Msg> msgs) {
        final ArrayList<Msg> messages = new ArrayList<>(msgs);
        messagesPerPath.put(path, messages);
    }


    private static class StyleUpdate {
        public final SpoofaxEditor editor;
        public final @Nullable String text;
        public final TextPresentation textPresentation;

        public StyleUpdate(SpoofaxEditor editor, @Nullable String text, TextPresentation textPresentation) {
            this.editor = editor;
            this.text = text;
            this.textPresentation = textPresentation;
        }
    }

    public void updateStyle(SpoofaxEditor editor, String text, Styling styling) {
        final TextPresentation textPresentation = styleUtils.createTextPresentation(styling, text.length());
        styleUpdates.add(new StyleUpdate(editor, text, textPresentation));
    }

    public void removeStyle(SpoofaxEditor editor, int textLength) {
        final TextPresentation textPresentation = styleUtils.createTextPresentation(Color.black, textLength);
        styleUpdates.add(new StyleUpdate(editor, null, textPresentation));
    }


    public Job update(@Nullable ISchedulingRule rule, @Nullable IProgressMonitor monitor) {
        final Job job = new Job("Spoofax workspace update") {
            @Override protected IStatus run(IProgressMonitor jobMonitor) {
                if((jobMonitor != null && jobMonitor.isCanceled()) || (monitor != null && monitor.isCanceled()))
                    return StatusUtils.cancel();

                try {
                    final ICoreRunnable parseMarkerUpdater = new IWorkspaceRunnable() {
                        @Override public void run(@Nullable IProgressMonitor workspaceMonitor) throws CoreException {
                            for(PPath path : pathsToClearRec) {
                                if(workspaceMonitor != null && workspaceMonitor.isCanceled())
                                    return;

                                logger.trace("Clearing messages for {}, recursively", path);
                                final IResource resource = toResource(path);
                                if(resource == null) {
                                    logger.error(
                                        "Failed to recursively clear messages for {}, it cannot be resolved to an Eclipse resource",
                                        path);
                                    continue;
                                }
                                MarkerUtils.clearAllRec(resource);
                            }
                            for(PPath path : pathsToClear) {
                                if(workspaceMonitor != null && workspaceMonitor.isCanceled())
                                    return;

                                logger.trace("Clearing messages for {}", path);
                                final IResource resource = toResource(path);
                                if(resource == null) {
                                    logger.error(
                                        "Failed to clear messages for {}, it cannot be resolved to an Eclipse resource",
                                        path);
                                    continue;
                                }
                                MarkerUtils.clearAll(resource);
                            }
                            for(Entry<PPath, ArrayList<Msg>> entry : messagesPerPath.entrySet()) {
                                if(workspaceMonitor != null && workspaceMonitor.isCanceled())
                                    return;

                                final PPath path = entry.getKey();
                                logger.trace("Updating messages for {}", path);
                                final IResource resource = toResource(path);
                                if(resource == null) {
                                    logger.error(
                                        "Failed to update messages for {}, it cannot be resolved to an Eclipse resource",
                                        path);
                                    continue;
                                }
                                for(Msg msg : entry.getValue()) {
                                    if(workspaceMonitor != null && workspaceMonitor.isCanceled())
                                        return;

                                    logger.trace("Adding message {} for {}", msg, resource);
                                    MarkerUtils.createMarker(resource, msg);
                                }
                            }
                        }
                    };
                    ResourcesPlugin.getWorkspace().run(parseMarkerUpdater, rule, IWorkspace.AVOID_UPDATE, monitor);
                } catch(CoreException e) {
                    return StatusUtils.error(e);
                }

                for(StyleUpdate styleUpdate : styleUpdates) {
                    if((jobMonitor != null && jobMonitor.isCanceled()) || (monitor != null && monitor.isCanceled()))
                        return StatusUtils.cancel();
                    final SpoofaxEditor editor = styleUpdate.editor;
                    logger.trace("Updating syntax styling for {}", editor);
                    editor.setStyleAsync(styleUpdate.textPresentation, styleUpdate.text, monitor);
                }

                return StatusUtils.success();
            }
        };
        job.setRule(rule);
        job.schedule();
        return job;
    }


    private @Nullable IResource toResource(PPath path) {
        return pathSrv.unresolve(path);
    }
}
