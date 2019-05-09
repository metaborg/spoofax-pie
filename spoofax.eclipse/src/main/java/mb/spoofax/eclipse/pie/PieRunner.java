package mb.spoofax.eclipse.pie;

import mb.common.message.Message;
import mb.common.message.MessageCollection;
import mb.common.style.Styling;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.pie.api.ExecException;
import mb.pie.api.Pie;
import mb.pie.api.PieSession;
import mb.pie.api.Task;
import mb.pie.api.exec.Cancelled;
import mb.pie.api.exec.NullCancelled;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.core.language.LanguageInstance;
import mb.spoofax.eclipse.editor.SpoofaxEditor;
import mb.spoofax.eclipse.resource.EclipseResourceKey;
import mb.spoofax.eclipse.resource.EclipseResourceRegistry;
import mb.spoofax.eclipse.util.MarkerUtils;
import mb.spoofax.eclipse.util.StyleUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.TextPresentation;

import javax.inject.Inject;
import java.util.HashSet;

public class PieRunner {
    private final Logger logger;
    private final Pie pie;
    private final EclipseResourceRegistry eclipseResourceRegistry;
    private final StyleUtil styleUtil;


    @Inject
    public PieRunner(LoggerFactory loggerFactory, Pie pie, EclipseResourceRegistry eclipseResourceRegistry, StyleUtil styleUtil) {
        this.logger = loggerFactory.create(getClass());
        this.pie = pie;
        this.eclipseResourceRegistry = eclipseResourceRegistry;
        this.styleUtil = styleUtil;
    }


    public <D extends IDocument & IDocumentExtension4> void addOrUpdateEditor(
        LanguageComponent languageComponent,
        IFile file,
        D document,
        SpoofaxEditor editor,
        @Nullable IProgressMonitor monitor
    ) throws ExecException, InterruptedException {
        final EclipseResourceKey resourceKey = new EclipseResourceKey(file);
        eclipseResourceRegistry.addDocumentOverride(resourceKey, document);

        try(final PieSession session = languageComponent.newPieSession()) {
            final LanguageInstance languageInstance = languageComponent.getLanguageInstance();

            // Set observer for styling task, and execute it if it has not been executed yet.
            final Task<@Nullable Styling> stylingTask = languageInstance.createStylingTask(resourceKey);
            final String text = document.get();
            pie.setObserver(stylingTask, (styling) -> {
                if(styling != null) {
                    final TextPresentation textPresentation = styleUtil.createTextPresentation(styling, text.length());
                    editor.setStyleAsync(textPresentation, text, monitor);
                }
            });
            if(!pie.hasBeenExecuted(stylingTask)) {
                session.requireTopDown(stylingTask, monitorCancelled(monitor));
            }

            // Set observer for messages task, and execute it if it has not been executed yet.
            final Task<MessageCollection> messagesTask = languageInstance.createMessagesTask(resourceKey);
            pie.setObserver(messagesTask, (messages) -> {
                try {
                    MarkerUtils.clearAll(file);
                    for(Message message : messages.getMessages()) {
                        MarkerUtils.createMarker(file, message);
                    }
                } catch(CoreException e) {
                    throw new RuntimeException("Clearing or setting markers failed unexpectedly", e);
                }
            });
            if(!pie.hasBeenExecuted(messagesTask)) {
                session.requireTopDown(messagesTask, monitorCancelled(monitor));
            }

            // Execute bottom-up build for changed file.
            final HashSet<ResourceKey> changedResources = new HashSet<>();
            changedResources.add(new EclipseResourceKey(file));
            session.requireBottomUp(changedResources);
        }
    }

    public void removeEditor(
        LanguageComponent languageComponent,
        IFile file
    ) {
        final EclipseResourceKey resourceKey = new EclipseResourceKey(file);
        eclipseResourceRegistry.removeDocumentOverride(resourceKey);

        final LanguageInstance languageInstance = languageComponent.getLanguageInstance();

        pie.removeObserver(languageInstance.createStylingTask(resourceKey));
        pie.removeObserver(languageInstance.createMessagesTask(resourceKey));
    }


    private static Cancelled monitorCancelled(@Nullable IProgressMonitor monitor) {
        if(monitor != null) {
            return new MonitorCancelToken(monitor);
        } else {
            return new NullCancelled();
        }
    }
}
