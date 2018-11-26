package mb.spoofax.runtime.eclipse.pipeline;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map.Entry;
import mb.fs.java.JavaFSPath;
import mb.log.api.Logger;
import mb.spoofax.api.message.Message;
import mb.spoofax.api.message.MessageCollection;
import mb.spoofax.api.style.Color;
import mb.spoofax.api.style.Styling;
import mb.spoofax.api.util.MultiHashMap;
import mb.spoofax.runtime.eclipse.editor.SpoofaxEditor;
import mb.spoofax.runtime.eclipse.util.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.TextPresentation;

public class WorkspaceUpdate {
    public static final LockRule lock = new LockRule("Workspace update lock");

    private final Logger logger;
    private final FileUtils fileUtils;
    private final StyleUtils styleUtils;

    private final ArrayList<JavaFSPath> pathsToClear = new ArrayList<>();
    private final ArrayList<JavaFSPath> pathsToClearRec = new ArrayList<>();
    private final MultiHashMap<String, Message> messagesPerPath = new MultiHashMap<>();
    private final ArrayList<StyleUpdate> styleUpdates = new ArrayList<>();


    @Inject public WorkspaceUpdate(Logger logger, FileUtils fileUtils, StyleUtils styleUtils) {
        this.logger = logger.forContext(getClass());
        this.fileUtils = fileUtils;
        this.styleUtils = styleUtils;
    }


    public void addClear(JavaFSPath path) {
        pathsToClear.add(path);
    }

    public void addClearRec(JavaFSPath path) {
        pathsToClearRec.add(path);
    }


    public void addMessages(Collection<Message> msgs, JavaFSPath path) {
        messagesPerPath.addAll(path.toString(), msgs);
    }

    public void addMessages(MessageCollection messageCollection) {
        // TODO: handle global messages
        messagesPerPath.addAll(messageCollection.containerMessages());
        messagesPerPath.addAll(messageCollection.documentMessages());
    }


    public void replaceMessages(ArrayList<Message> messages, JavaFSPath path) {
        messagesPerPath.replaceAll(path.toString(), messages);
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
                            for(JavaFSPath path : pathsToClearRec) {
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
                            for(JavaFSPath path : pathsToClear) {
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
                            for(Entry<String, ArrayList<Message>> entry : messagesPerPath.entrySet()) {
                                if(workspaceMonitor != null && workspaceMonitor.isCanceled())
                                    return;

                                final String pathStr = entry.getKey();
                                final JavaFSPath path = new JavaFSPath(pathStr);
                                logger.trace("Updating messages for {}", path);
                                final IResource resource = toResource(path);
                                if(resource == null) {
                                    logger.error(
                                        "Failed to update messages for {}, it cannot be resolved to an Eclipse resource",
                                        path);
                                    continue;
                                }
                                for(Message msg : entry.getValue()) {
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


    private @Nullable IResource toResource(JavaFSPath path) {
        return fileUtils.toResource(path);
    }
}
