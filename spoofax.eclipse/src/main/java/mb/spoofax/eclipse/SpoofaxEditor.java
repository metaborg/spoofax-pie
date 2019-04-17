package mb.spoofax.eclipse;

import mb.log.api.Logger;
import mb.spoofax.eclipse.util.FileUtils;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextViewerExtension4;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;

public class SpoofaxEditor extends TextEditor {
    private final class DocumentListener implements IDocumentListener {
        @Override public void documentAboutToBeChanged(@NonNull DocumentEvent event) {
            // Don't care about this event.
        }

        @Override public void documentChanged(@NonNull DocumentEvent event) {
            scheduleJob(false);
        }
    }


    private final PresentationMerger presentationMerger = new PresentationMerger();

    private @MonotonicNonNull IJobManager jobManager = null;
    private @MonotonicNonNull Logger logger = null;
    private @MonotonicNonNull FileUtils fileUtils = null;
//    private PipelineAdapter pipelineAdapter;

    private @Nullable IEditorInput input = null;
    private @Nullable String inputName = null;
    private @Nullable IDocument document = null;

    private @Nullable DocumentListener documentListener;
    private @Nullable ISourceViewer sourceViewer;

    private @Nullable IFile file;
    private @Nullable IProject project;


    public void setStyleAsync(TextPresentation textPresentation, @Nullable String text, @Nullable IProgressMonitor monitor) {
        presentationMerger.set(textPresentation);
        // Update textPresentation on the main thread, required by Eclipse.
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                if(monitor != null && monitor.isCanceled()) {
                    return;
                }
                // Also cancel if text presentation is not valid for current text any more.
                if(document == null || sourceViewer == null || (text != null && !document.get().equals(text))) {
                    return;
                }
                sourceViewer.changeTextPresentation(textPresentation, true);
            }
        });
    }


    @Override protected void initializeEditor() {
        super.initializeEditor();

        this.jobManager = Job.getJobManager();

        final SpoofaxFacade spoofaxFacade = SpoofaxPlugin.spoofaxFacade();
        final Injector injector = spoofaxFacade.injector;

        this.logger = injector.getInstance(Logger.class);
        this.fileUtils = injector.getInstance(FileUtils.class);
        this.pipelineAdapter = injector.getInstance(PipelineAdapter.class);

        setDocumentProvider(new SpoofaxDocumentProvider());
        setSourceViewerConfiguration(new SourceViewerConfiguration());
    }

    @Override
    protected ISourceViewer createSourceViewer(@NonNull Composite parent, @NonNull IVerticalRuler ruler, int styles) {
        input = getEditorInput();
        document = getDocumentProvider().getDocument(input);

        final @Nullable IFile inputFile = fileUtils.toFile(input);
        if(inputFile != null) {
            this.inputName = inputFile.toString();
            this.file = inputFile;
            this.project = inputFile.getProject();
        } else {
            this.inputName = input.getName();
            this.file = null;
            this.project = null;
            logger.warn("File for editor on {} is null, cannot update the editor", input);
        }

        documentListener = new DocumentListener();
        document.addDocumentListener(documentListener);

        sourceViewer = super.createSourceViewer(parent, ruler, styles);
        final SourceViewerDecorationSupport decorationSupport = getSourceViewerDecorationSupport(sourceViewer);
        configureSourceViewerDecorationSupport(decorationSupport);

        ((ITextViewerExtension4) sourceViewer).addTextPresentationListener(presentationMerger);

        if(file != null && project == null) {
            pipelineAdapter.addEditor(this, document.get(), file, project);
        }

        scheduleJob(true);

        return sourceViewer;
    }

    @Override public void dispose() {
        cancelJobs(input);

        if(documentListener != null) {
            document.removeDocumentListener(documentListener);
        }

        pipelineAdapter.removeEditor(this);

        input = null;
        document = null;
        documentListener = null;

        super.dispose();
    }


    private void scheduleJob(boolean initialUpdate) {
        cancelJobs(input);
        if(file == null || project == null) {
            return;
        }
        final Job job = new EditorUpdateJob(logger, pipelineAdapter, this, document.get(), file, project, input);
        job.schedule(initialUpdate ? 0 : 300);
    }

    private void cancelJobs(IEditorInput specificInput) {
        final Job[] existingJobs = jobManager.find(specificInput);
        if(existingJobs.length > 0) {
            logger.trace("Cancelling editor update jobs for {}", specificInput);
            for(Job job : existingJobs) {
                job.cancel();
            }
        }
    }
}
