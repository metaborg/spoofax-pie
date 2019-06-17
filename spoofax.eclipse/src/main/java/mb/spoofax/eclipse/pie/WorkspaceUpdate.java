package mb.spoofax.eclipse.pie;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.style.Color;
import mb.common.style.Styling;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.resource.Resource;
import mb.spoofax.eclipse.editor.SpoofaxEditor;
import mb.spoofax.eclipse.resource.EclipseResourceRegistry;
import mb.spoofax.eclipse.util.MarkerUtil;
import mb.spoofax.eclipse.util.StatusUtil;
import mb.spoofax.eclipse.util.StyleUtil;
import mb.spoofax.eclipse.util.UncheckedCoreException;
import org.checkerframework.checker.nullness.qual.Nullable;
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

import javax.inject.Inject;
import java.util.ArrayList;

public class WorkspaceUpdate {
    public static final LockRule lock = new LockRule("Workspace update lock");

    private final Logger logger;
    private final EclipseResourceRegistry resourceRegistry;
    private final StyleUtil styleUtil;

    private final ArrayList<IResource> clear = new ArrayList<>();
    private final ArrayList<IResource> clearRecursively = new ArrayList<>();
    private final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();
    private final ArrayList<StyleUpdate> styleUpdates = new ArrayList<>();


    @Inject
    public WorkspaceUpdate(LoggerFactory loggerFactory, EclipseResourceRegistry resourceRegistry, StyleUtil styleUtil) {
        this.logger = loggerFactory.create(getClass());
        this.resourceRegistry = resourceRegistry;
        this.styleUtil = styleUtil;
    }


    public void clearMessages(IResource resource) {
        clear.add(resource);
    }

    public void clearMessagesRecursively(IResource resource) {
        clearRecursively.add(resource);
    }


    public void addMessages(KeyedMessages messages) {
        messagesBuilder.addMessages(messages);
    }

    public void replaceMessages(KeyedMessages messages) {
        messagesBuilder.replaceMessages(messages);
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
        final TextPresentation textPresentation = styleUtil.createTextPresentation(styling, text.length());
        styleUpdates.add(new StyleUpdate(editor, text, textPresentation));
    }

    public void removeStyle(SpoofaxEditor editor, int textLength) {
        final TextPresentation textPresentation = styleUtil.createTextPresentation(Color.black, textLength);
        styleUpdates.add(new StyleUpdate(editor, null, textPresentation));
    }


    public Job update(@Nullable ISchedulingRule rule, @Nullable IProgressMonitor monitor) {
        final Job job = new Job("Spoofax workspace update") {
            @Override protected IStatus run(@Nullable IProgressMonitor jobMonitor) {
                if((jobMonitor != null && jobMonitor.isCanceled()) || (monitor != null && monitor.isCanceled())) {
                    return StatusUtil.cancel();
                }

                logger.trace("Running workspace update job");

                final KeyedMessages messages = messagesBuilder.build();

                try {
                    final ICoreRunnable parseMarkerUpdater = new IWorkspaceRunnable() {
                        @Override public void run(@Nullable IProgressMonitor workspaceMonitor) throws CoreException {
                            for(IResource resource : clearRecursively) {
                                if(workspaceMonitor != null && workspaceMonitor.isCanceled()) return;
                                MarkerUtil.clearAllRec(resource);
                            }
                            for(IResource resource : clear) {
                                if(workspaceMonitor != null && workspaceMonitor.isCanceled()) return;
                                MarkerUtil.clearAll(resource);
                            }
                            try {
                                messages.accept((text, exception, severity, resourceKey, region) -> {
                                    if(workspaceMonitor != null && workspaceMonitor.isCanceled()) return false;
                                    try {
                                        if(resourceKey == null) {
                                            logger.warn(
                                                "Cannot create marker with text '" + text + "'; it has no corresponding resource");
                                            return true;
                                        }
                                        final Resource resource = resourceRegistry.getResource(resourceKey.getId());
                                        final @Nullable IResource eclipseResource =
                                            resourceRegistry.getWrappedEclipseResource(resource);
                                        if(eclipseResource == null) {
                                            logger.warn(
                                                "Cannot create marker with text '" + text + "' onto resource '" + resource + "'; it does not have a corresponding Eclipse resource");
                                            return true;
                                        }
                                        MarkerUtil.createMarker(text, severity, eclipseResource, region);
                                    } catch(CoreException e) {
                                        throw new UncheckedCoreException(e);
                                    }
                                    return true;
                                });
                            } catch(UncheckedCoreException e) {
                                throw e.getCause();
                            }
                        }
                    };
                    ResourcesPlugin.getWorkspace().run(parseMarkerUpdater, rule, IWorkspace.AVOID_UPDATE, monitor);
                } catch(CoreException e) {
                    final String message = "Running marker update failed unexpectedly";
                    logger.error(message, e);
                    return StatusUtil.error(message, e);
                }

                for(StyleUpdate styleUpdate : styleUpdates) {
                    if((jobMonitor != null && jobMonitor.isCanceled()) || (monitor != null && monitor.isCanceled())) {
                        return StatusUtil.cancel();
                    }
                    final SpoofaxEditor editor = styleUpdate.editor;
                    editor.setStyleAsync(styleUpdate.textPresentation, styleUpdate.text, monitor);
                }

                return StatusUtil.success();
            }
        };
        job.setRule(rule);
        job.schedule();
        return job;
    }
}
