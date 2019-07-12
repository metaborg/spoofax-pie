package mb.spoofax.eclipse.pie;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.style.Color;
import mb.common.style.Styling;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.resource.ResourceKey;
import mb.resource.ResourceService;
import mb.spoofax.eclipse.editor.SpoofaxEditor;
import mb.spoofax.eclipse.resource.WrapsEclipseResource;
import mb.spoofax.eclipse.util.MarkerUtil;
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
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.text.TextPresentation;

import javax.inject.Inject;
import java.util.ArrayList;

public class WorkspaceUpdate {
    public static final LockRule lock = new LockRule("Workspace update lock");

    private final Logger logger;
    private final ResourceService resourceService;
    private final StyleUtil styleUtil;

    private final ArrayList<ResourceKey> clear = new ArrayList<>();
    private final ArrayList<ResourceKey> clearRecursively = new ArrayList<>();
    private final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();
    private final ArrayList<StyleUpdate> styleUpdates = new ArrayList<>();


    @Inject
    public WorkspaceUpdate(LoggerFactory loggerFactory, ResourceService resourceService, StyleUtil styleUtil) {
        this.logger = loggerFactory.create(getClass());
        this.resourceService = resourceService;
        this.styleUtil = styleUtil;
    }


    public void reset() {
        clear.clear();
        clearRecursively.clear();
        messagesBuilder.clearAll();
        styleUpdates.clear();
    }


    public void clearMessages(ResourceKey resource) {
        clear.add(resource);
    }

    public void clearMessagesRecursively(ResourceKey resource) {
        clearRecursively.add(resource);
    }


    public void addMessages(KeyedMessages messages) {
        messagesBuilder.addMessages(messages);
    }

    public void replaceMessages(KeyedMessages messages) {
        for(@Nullable ResourceKey resource : messages.getResources()) {
            if(resource != null) {
                clearMessages(resource);
            }
        }
        messagesBuilder.replaceMessages(messages);
    }


    private static class StyleUpdate {
        public final SpoofaxEditor editor;
        public final @Nullable String text;
        public final int textLength;
        public final TextPresentation textPresentation;

        public StyleUpdate(SpoofaxEditor editor, @Nullable String text, int textLength, TextPresentation textPresentation) {
            this.editor = editor;
            this.text = text;
            this.textLength = textLength;
            this.textPresentation = textPresentation;
        }
    }

    public void updateStyle(SpoofaxEditor editor, String text, Styling styling) {
        final TextPresentation textPresentation = styleUtil.createTextPresentation(styling, text.length());
        styleUpdates.add(new StyleUpdate(editor, text, text.length(), textPresentation));
    }

    public void removeStyle(SpoofaxEditor editor, int textLength) {
        final TextPresentation textPresentation = styleUtil.createTextPresentation(Color.black, textLength);
        styleUpdates.add(new StyleUpdate(editor, null, textLength, textPresentation));
    }


    public void update(@Nullable ISchedulingRule rule, @Nullable IProgressMonitor monitor) {
        if(monitor != null && monitor.isCanceled()) {
            return;
        }
        final KeyedMessages messages = messagesBuilder.build();
        try {
            final ICoreRunnable makerUpdate = (IWorkspaceRunnable) workspaceMonitor -> {
                for(ResourceKey resourceKey : clearRecursively) {
                    if(workspaceMonitor != null && workspaceMonitor.isCanceled()) return;
                    try {
                        final WrapsEclipseResource wrapsEclipseResource = resourceService.getResource(resourceKey);
                        final @Nullable IResource resource = wrapsEclipseResource.getWrappedEclipseResource();
                        if(resource != null) {
                            MarkerUtil.clearAllRec(resource);
                        } else {
                            throw new RuntimeException("Cannot recursively clear markers for resource with key '" + resourceKey + "', resource '" + wrapsEclipseResource + "' was found but it does not have a corresponding Eclipse resource");
                        }
                    } catch(ClassCastException e) {
                        throw new RuntimeException("Cannot recursively clear markers for resource with key '" + resourceKey + "', it is not an Eclipse resource", e);
                    }
                }
                for(ResourceKey resourceKey : clear) {
                    if(workspaceMonitor != null && workspaceMonitor.isCanceled()) return;
                    try {
                        final WrapsEclipseResource wrapsEclipseResource = resourceService.getResource(resourceKey);
                        final @Nullable IResource resource = wrapsEclipseResource.getWrappedEclipseResource();
                        if(resource != null) {
                            MarkerUtil.clearAll(resource);
                        } else {
                            throw new RuntimeException("Cannot clear markers for resource with key '" + resourceKey + "', resource '" + wrapsEclipseResource + "' was found but it does not have a corresponding Eclipse resource");
                        }
                    } catch(ClassCastException e) {
                        throw new RuntimeException("Cannot clear markers for resource with key '" + resourceKey + "', it is not an Eclipse resource", e);
                    }
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

                            try {
                                final WrapsEclipseResource wrapsEclipseResource = resourceService.getResource(resourceKey);
                                final @Nullable IResource resource = wrapsEclipseResource.getWrappedEclipseResource();
                                if(resource != null) {
                                    MarkerUtil.createMarker(text, severity, resource, region);
                                } else {
                                    throw new RuntimeException("Cannot set markers for resource with key '" + resourceKey + "', resource '" + wrapsEclipseResource + "' was found but it does not have a corresponding Eclipse resource");
                                }
                            } catch(ClassCastException e) {
                                throw new RuntimeException("Cannot set markers for resource with key '" + resourceKey + "', it is not an Eclipse resource", e);
                            }
                        } catch(CoreException e) {
                            throw new UncheckedCoreException(e);
                        }
                        return true;
                    });
                } catch(UncheckedCoreException e) {
                    throw e.getCause();
                }
            };
            ResourcesPlugin.getWorkspace().run(makerUpdate, rule, IWorkspace.AVOID_UPDATE, monitor);
        } catch(CoreException e) {
            logger.error("Running marker update failed unexpectedly", e);
            return;
        }

        for(StyleUpdate styleUpdate : styleUpdates) {
            if(monitor != null && monitor.isCanceled()) {
                return;
            }
            final SpoofaxEditor editor = styleUpdate.editor;
            editor.setStyleAsync(styleUpdate.textPresentation, styleUpdate.text, styleUpdate.textLength, monitor);
        }
    }
}
