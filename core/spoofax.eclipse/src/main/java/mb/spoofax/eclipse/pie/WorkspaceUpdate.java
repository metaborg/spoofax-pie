package mb.spoofax.eclipse.pie;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Message;
import mb.common.style.Styling;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.resource.ResourceKey;
import mb.spoofax.eclipse.EclipseLanguageComponent;
import mb.spoofax.eclipse.editor.SpoofaxEditor;
import mb.spoofax.eclipse.util.MarkerUtil;
import mb.spoofax.eclipse.util.ResourceUtil;
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
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;

public class WorkspaceUpdate {
    @Singleton
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

        public WorkspaceUpdate create(EclipseLanguageComponent languageComponent) {
            return new WorkspaceUpdate(loggerFactory, resourceUtil, styleUtil, languageComponent);
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

    private final EclipseLanguageComponent languageComponent;

    private final ArrayList<ClearMessages> clears = new ArrayList<>();
    private final KeyedMessagesBuilder keyedMessagesBuilder = new KeyedMessagesBuilder();
    private final ArrayList<StyleUpdate> styleUpdates = new ArrayList<>();


    public WorkspaceUpdate(LoggerFactory loggerFactory, ResourceUtil resourceService, StyleUtil styleUtil, EclipseLanguageComponent languageComponent) {
        this.logger = loggerFactory.create(getClass());
        this.resourceUtil = resourceService;
        this.styleUtil = styleUtil;
        this.languageComponent = languageComponent;
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

    public void addMessages(KeyedMessages keyedMessages, @Nullable ResourceKey defaultOrigin) {
        if(defaultOrigin != null) {
            keyedMessagesBuilder.addMessagesWithDefaultKey(keyedMessages, defaultOrigin);
        } else {
            keyedMessagesBuilder.addMessages(keyedMessages);
        }
    }


    public void replaceMessages(ResourceKey origin, Collection<? extends Message> messages) {
        clearMessages(origin, false);
        addMessages(origin, messages);
    }

    public void replaceMessages(KeyedMessages keyedMessages, @Nullable ResourceKey defaultOrigin) {
        for(ResourceKey origin : keyedMessages.getKeys()) {
            clearMessages(origin, false);
        }
        addMessages(keyedMessages, defaultOrigin);
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
        final TextPresentation textPresentation = styleUtil.createDefaultTextPresentation(textLength);
        styleUpdates.add(new StyleUpdate(editor, null, textLength, textPresentation));
    }


    public void update(IResource defaultOrigin, @Nullable ISchedulingRule rule, @Nullable IProgressMonitor monitor) {
        if(monitor != null && monitor.isCanceled()) return;
        final KeyedMessages keyedMessages = keyedMessagesBuilder.build();
        final ICoreRunnable makerUpdate = (IWorkspaceRunnable)workspaceMonitor -> {
            for(ClearMessages clear : clears) {
                if(workspaceMonitor != null && workspaceMonitor.isCanceled()) return;
                final IResource resource = resourceUtil.getEclipseResource(clear.origin);
                MarkerUtil.clear(languageComponent.getEclipseIdentifiers(), resource, clear.recursive);
            }
            try {
                if(workspaceMonitor == null || !workspaceMonitor.isCanceled()) {
                    keyedMessages.getMessagesWithKey().forEach(entry -> {
                        ResourceKey resourceKey = entry.getKey();
                        entry.getValue().forEach(message -> {
                            final IResource resource = resourceUtil.getEclipseResource(resourceKey);
                            try {
                                MarkerUtil.create(languageComponent.getEclipseIdentifiers(), message.text, message.severity, resource, message.region);
                            } catch(CoreException e) {
                                throw new UncheckedCoreException(e);
                            }
                        });
                    });
                    keyedMessages.getMessagesWithoutKey().forEach(message -> {
                        try {
                            MarkerUtil.create(languageComponent.getEclipseIdentifiers(), message.text, message.severity, defaultOrigin, message.region);
                        } catch(CoreException e) {
                            throw new UncheckedCoreException(e);
                        }
                    });
                }
            } catch(UncheckedCoreException e) {
                throw e.getCause();
            }
        };
        try {
            ResourcesPlugin.getWorkspace().run(makerUpdate, rule, IWorkspace.AVOID_UPDATE, monitor);
        } catch(CoreException e) {
            logger.error("Running marker update failed unexpectedly", e);
            return;
        }

        for(StyleUpdate styleUpdate : styleUpdates) {
            if(monitor != null && monitor.isCanceled()) return;
            final SpoofaxEditor editor = styleUpdate.editor;
            editor.setStyleAsync(styleUpdate.textPresentation, styleUpdate.text, styleUpdate.textLength, monitor);
        }
    }
}
