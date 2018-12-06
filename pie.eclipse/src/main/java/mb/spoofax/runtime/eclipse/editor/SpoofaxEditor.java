package mb.spoofax.runtime.eclipse.editor;

import com.google.inject.Injector;
import mb.log.api.Logger;
import mb.spoofax.api.SpoofaxFacade;
import mb.spoofax.runtime.eclipse.SpoofaxPlugin;
import mb.spoofax.runtime.eclipse.pipeline.PipelineAdapter;
import mb.spoofax.runtime.eclipse.util.FileUtils;
import mb.spoofax.runtime.eclipse.util.Nullable;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;

public class SpoofaxEditor extends TextEditor {
    private final class DocumentListener implements IDocumentListener {
        @Override public void documentAboutToBeChanged(DocumentEvent event) {
            // Don't care about this event.
        }

        @Override public void documentChanged(DocumentEvent event) {
            scheduleJob(false);
        }
    }

    private final PresentationMerger presentationMerger;

    private IJobManager jobManager;
    private Logger logger;
    private FileUtils fileUtils;
    private PipelineAdapter pipelineAdapter;

    private IEditorInput input;
    private String inputName;
    private IDocument document;
    private @Nullable IFile file;
    private @Nullable IProject project;
    private DocumentListener documentListener;
    private ISourceViewer sourceViewer;


    public SpoofaxEditor() {
        this.presentationMerger = new PresentationMerger();
    }


    public String text() {
        return document.get();
    }

    public String name() {
        return inputName;
    }

    public ISourceViewer sourceViewer() {
        return sourceViewer;
    }


    public void setStyleAsync(final TextPresentation textPresentation, @Nullable final String text,
        @Nullable final IProgressMonitor monitor) {
        presentationMerger.set(textPresentation);
        // Update textPresentation on the main thread, required by Eclipse.
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                if(monitor != null && monitor.isCanceled())
                    return;
                // Also cancel if text presentation is not valid for current text any more.
                if(document == null || (text != null && !document.get().equals(text))) {
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

        setDocumentProvider(new DocumentProvider(logger, fileUtils));
        setSourceViewerConfiguration(new SpoofaxSourceViewerConfiguration());
    }

    @Override protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
        input = getEditorInput();
        document = getDocumentProvider().getDocument(input);

        final IFile inputFile = fileUtils.toFile(input);
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
            pipelineAdapter.addEditor(this, text(), file, project);
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
