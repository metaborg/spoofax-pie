package mb.spoofax.eclipse.pie;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Message;
import mb.common.style.Styling;
import mb.common.util.ExceptionPrinter;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.resource.ResourceKey;
import mb.resource.ResourceRuntimeException;
import mb.spoofax.core.platform.PlatformScope;
import mb.spoofax.eclipse.EclipseIdentifiers;
import mb.spoofax.eclipse.editor.SpoofaxEditorBase;
import mb.spoofax.eclipse.util.MarkerUtil;
import mb.spoofax.eclipse.util.ResourceUtil;
import mb.spoofax.eclipse.util.StyleUtil;
import mb.spoofax.eclipse.util.UncheckedCoreException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.text.TextPresentation;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;

public class WorkspaceUpdate {
    @PlatformScope
    public static class Factory {
        private final LoggerFactory loggerFactory;
        private final ResourceUtil resourceUtil;
        private final StyleUtil styleUtil;

        @Inject
        public Factory(LoggerFactory loggerFactory, ResourceUtil resourceUtil, StyleUtil styleUtil) {
            this.loggerFactory = loggerFactory;
            this.resourceUtil = resourceUtil;
            this.styleUtil = styleUtil;
        }

        public WorkspaceUpdate create(EclipseIdentifiers eclipseIdentifiers) {
            return new WorkspaceUpdate(loggerFactory, resourceUtil, styleUtil, eclipseIdentifiers);
        }
    }

    private static class ClearMessages {
        public final ResourceKey origin;
        public final boolean recursive;

        private ClearMessages(ResourceKey origin, boolean recursive) {
            this.origin = origin;
            this.recursive = recursive;
        }
    }


    private final Logger logger;
    private final ResourceUtil resourceUtil;
    private final StyleUtil styleUtil;

    private final EclipseIdentifiers eclipseIdentifiers;

    private final ArrayList<ClearMessages> clears = new ArrayList<>();
    private final KeyedMessagesBuilder keyedMessagesBuilder = new KeyedMessagesBuilder();
    private final ArrayList<StyleUpdate> styleUpdates = new ArrayList<>();


    public WorkspaceUpdate(LoggerFactory loggerFactory, ResourceUtil resourceService, StyleUtil styleUtil, EclipseIdentifiers eclipseIdentifiers) {
        this.logger = loggerFactory.create(getClass());
        this.resourceUtil = resourceService;
        this.styleUtil = styleUtil;
        this.eclipseIdentifiers = eclipseIdentifiers;
    }


    public void reset() {
        clears.clear();
        keyedMessagesBuilder.clearAll();
        styleUpdates.clear();
    }


    public void clearMessages(ResourceKey origin, boolean recursive) {
        clears.add(new ClearMessages(origin, recursive));
    }


    public void addMessages(ResourceKey origin, Collection<? extends Message> messages) {
        keyedMessagesBuilder.addMessages(origin, messages);
    }

    public void addMessages(KeyedMessages keyedMessages, @Nullable ResourceKey fallbackResource) {
        if(fallbackResource != null) {
            keyedMessagesBuilder.addMessagesWithFallbackKey(keyedMessages, fallbackResource);
        } else {
            keyedMessagesBuilder.addMessages(keyedMessages);
        }
    }


    public void replaceMessages(ResourceKey origin, Collection<? extends Message> messages) {
        clearMessages(origin, false);
        addMessages(origin, messages);
    }

    public void replaceMessages(KeyedMessages keyedMessages, @Nullable ResourceKey fallbackResource) {
        for(ResourceKey origin : keyedMessages.getKeys()) {
            clearMessages(origin, false);
        }
        final @Nullable ResourceKey resourceForMessagesWithoutKey = keyedMessages.getResourceForMessagesWithoutKeys();
        if(resourceForMessagesWithoutKey != null && !keyedMessages.getMessagesWithoutKey().isEmpty()) {
            clearMessages(resourceForMessagesWithoutKey, false);
        }
        addMessages(keyedMessages, fallbackResource);
    }


    private static class StyleUpdate {
        public final SpoofaxEditorBase editor;
        public final @Nullable String text;
        public final int textLength;
        public final TextPresentation textPresentation;

        public StyleUpdate(SpoofaxEditorBase editor, @Nullable String text, int textLength, TextPresentation textPresentation) {
            this.editor = editor;
            this.text = text;
            this.textLength = textLength;
            this.textPresentation = textPresentation;
        }
    }

    public void updateStyle(SpoofaxEditorBase editor, String text, Styling styling) {
        final TextPresentation textPresentation = styleUtil.createTextPresentation(styling, text.length());
        styleUpdates.add(new StyleUpdate(editor, text, text.length(), textPresentation));
    }

    public void removeStyle(SpoofaxEditorBase editor, int textLength) {
        final TextPresentation textPresentation = styleUtil.createDefaultTextPresentation(textLength);
        styleUpdates.add(new StyleUpdate(editor, null, textLength, textPresentation));
    }


    public ICoreRunnable createMarkerUpdate() {
        final KeyedMessages keyedMessages = keyedMessagesBuilder.build();
        return workspaceMonitor -> {
            final ExceptionPrinter exceptionPrinter = new ExceptionPrinter();
            for(ClearMessages clear : clears) {
                if(workspaceMonitor != null && workspaceMonitor.isCanceled()) return;
                try {
                    final IResource resource = resourceUtil.getEclipseResource(clear.origin);
                    if(!resource.exists() || !enclosingProjectIsOpen(resource)) {
                        logger.error("Cannot clear markers for resource '{}'; resource does not exist, or its enclosing project is not open or does not exist", resource);
                        continue;
                    }
                    MarkerUtil.clear(eclipseIdentifiers, resource, clear.recursive);
                } catch(ResourceRuntimeException e) {
                    logger.error("Cannot clear markers for resource '{}'; getting Eclipse resource failed unexpectedly", e, clear.origin);
                }
            }
            try {
                if(workspaceMonitor == null || !workspaceMonitor.isCanceled()) {
                    keyedMessages.getMessagesWithKey().forEach(entry -> createMarkers(exceptionPrinter, entry.getKey(), entry.getValue()));
                    final @Nullable ResourceKey resourceForMessagesWithoutKey = keyedMessages.getResourceForMessagesWithoutKeys();
                    if(resourceForMessagesWithoutKey != null) {
                        createMarkers(exceptionPrinter, resourceForMessagesWithoutKey, keyedMessages.getMessagesWithoutKey());
                    }
                }
            } catch(UncheckedCoreException e) {
                throw e.getCause();
            }
        };
    }

    private void createMarkers(ExceptionPrinter exceptionPrinter, ResourceKey resourceKey, Iterable<Message> messages) {
        try {
            final IResource resource = resourceUtil.getEclipseResource(resourceKey);
            if(!resource.exists() || !enclosingProjectIsOpen(resource)) {
                logger.error("Cannot set markers for resource '{}'; resource does not exist, or its enclosing project is not open or does not exist", resource);
                return;
            }
            if(!(resource instanceof IContainer)) { // HACK: do not create errors on containers for now.
                messages.forEach(m -> createMarker(exceptionPrinter, m, resource));
            }
        } catch(ResourceRuntimeException e) {
            logger.error("Cannot create markers for resource '{}'; getting Eclipse resource failed unexpectedly", e, resourceKey);
        }
    }

    private void createMarker(ExceptionPrinter exceptionPrinter, Message message, IResource resource) {
        try {
            MarkerUtil.create(eclipseIdentifiers, exceptionPrinter, message.text, message.severity, resource, message.region, message.exception);
        } catch(CoreException e) {
            throw new UncheckedCoreException(e);
        }
    }

    public void update(@Nullable ISchedulingRule rule, @Nullable IProgressMonitor monitor) {
        if(monitor != null && monitor.isCanceled()) return;
        final ICoreRunnable makerUpdate = createMarkerUpdate();
        try {
            ResourcesPlugin.getWorkspace().run(makerUpdate, rule, IWorkspace.AVOID_UPDATE, monitor);
        } catch(CoreException e) {
            logger.error("Running marker update failed unexpectedly", e);
            return;
        }

        for(StyleUpdate styleUpdate : styleUpdates) {
            if(monitor != null && monitor.isCanceled()) return;
            final SpoofaxEditorBase editor = styleUpdate.editor;
            editor.setStyleAsync(styleUpdate.textPresentation, styleUpdate.text, styleUpdate.textLength, monitor);
        }
    }

    private static boolean enclosingProjectIsOpen(IResource resource) {
        final @Nullable IProject project = resource.getProject();
        if(project == null) return true;
        return project.isOpen();
    }
}
