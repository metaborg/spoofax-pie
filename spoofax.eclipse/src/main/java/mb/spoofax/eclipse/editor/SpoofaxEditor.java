package mb.spoofax.eclipse.editor;

import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.eclipse.SpoofaxEclipseComponent;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.spoofax.eclipse.pie.PieRunner;
import mb.spoofax.eclipse.util.FileUtil;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IFile;
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

public abstract class SpoofaxEditor extends TextEditor {
    private final class DocumentListener implements IDocumentListener {
        @Override public void documentAboutToBeChanged(@NonNull DocumentEvent event) {
            // Don't care about this event.
        }

        @Override public void documentChanged(@NonNull DocumentEvent event) {
            scheduleJob(false);
        }
    }


    private final PresentationMerger presentationMerger = new PresentationMerger();

    /*
    Do not initialize any of the following fields to null, as TextEditor's constructor will call 'initializeEditor' to
    initialize several fields, which will then be set back to null when initialized here.
    */

    // Set in initializeEditor, never null after that.
    @SuppressWarnings("NullableProblems") private @MonotonicNonNull IJobManager jobManager;
    @SuppressWarnings("NullableProblems") private @MonotonicNonNull LoggerFactory loggerFactory;
    @SuppressWarnings("NullableProblems") private @MonotonicNonNull Logger logger;
    @SuppressWarnings("NullableProblems") private @MonotonicNonNull FileUtil fileUtil;
    @SuppressWarnings("NullableProblems") private @MonotonicNonNull PieRunner pieRunner;

    // Set in createSourceViewer, unset in dispose, may never be null otherwise.
    private @Nullable IEditorInput input;
    private @Nullable String inputName;
    private @Nullable IDocument document;
    private @Nullable DocumentListener documentListener;
    private @Nullable ISourceViewer sourceViewer;

    // Set in createSourceViewer, but may be null if there is no associated file for the editor input.
    private @Nullable IFile file;


    public void setStyleAsync(TextPresentation textPresentation, @Nullable String text, @Nullable IProgressMonitor monitor) {
        presentationMerger.set(textPresentation);
        // Update textPresentation on the main thread, required by Eclipse.
        Display.getDefault().asyncExec(() -> {
            // Cancel if monitor is cancelled.
            if(monitor != null && monitor.isCanceled()) {
                return;
            }
            // Cancel if editor has been closed.
            if(document == null || sourceViewer == null) {
                return;
            }
            // Cancel if text presentation is not valid for current text any more.
            if(text != null && !document.get().equals(text)) {
                return;
            }
            sourceViewer.changeTextPresentation(textPresentation, true);
        });
    }


    protected abstract LanguageComponent getLanguageComponent();


    @Override protected void initializeEditor() {
        super.initializeEditor();

        this.jobManager = Job.getJobManager();

        final SpoofaxEclipseComponent component = SpoofaxPlugin.getComponent();
        this.loggerFactory = component.getLoggerFactory();
        this.logger = loggerFactory.create(getClass());
        this.fileUtil = component.getFileUtils();
        this.pieRunner = component.getPieRunner();

        setDocumentProvider(new SpoofaxDocumentProvider());
        setSourceViewerConfiguration(new SourceViewerConfiguration());
    }

    @Override
    protected ISourceViewer createSourceViewer(@NonNull Composite parent, @NonNull IVerticalRuler ruler, int styles) {
        input = getEditorInput();
        document = getDocumentProvider().getDocument(input);

        final @Nullable IFile inputFile = fileUtil.toFile(input);
        if(inputFile != null) {
            this.inputName = inputFile.toString();
            this.file = inputFile;
        } else {
            this.inputName = input.getName();
            this.file = null;
            logger.warn("File for editor on {} is null, cannot update the editor", input);
        }

        documentListener = new DocumentListener();
        document.addDocumentListener(documentListener);

        sourceViewer = super.createSourceViewer(parent, ruler, styles);
        final SourceViewerDecorationSupport decorationSupport = getSourceViewerDecorationSupport(sourceViewer);
        configureSourceViewerDecorationSupport(decorationSupport);

        ((ITextViewerExtension4) sourceViewer).addTextPresentationListener(presentationMerger);

        scheduleJob(true);

        return sourceViewer;
    }

    @Override public void dispose() {
        if(input != null) {
            cancelJobs(input);
        }

        if(documentListener != null) {
            document.removeDocumentListener(documentListener);
        }

        if(file != null) {
            pieRunner.removeEditor(getLanguageComponent(), file);
        } else {
            logger.error("Cannot remove editor '{}' from PieRunner, as input '{}' was not resolved to a file", this,
                input);
        }

        input = null;
        document = null;
        documentListener = null;

        super.dispose();
    }


    private void scheduleJob(boolean initialUpdate) {
        cancelJobs(input);
        if(file == null) {
            logger.error("Cannot schedule editor update job for editor '{}', as input '{}' was not resolved to a file",
                this, input);
        }
        final Job job = new EditorUpdateJob(loggerFactory, pieRunner, getLanguageComponent(), file, document, this);
        job.setRule(file);
        job.schedule(initialUpdate ? 0 : 300);
    }

    private void cancelJobs(IEditorInput specificInput) {
        final Job[] existingJobs = jobManager.find(specificInput);
        if(existingJobs.length > 0) {
            logger.trace("Cancelling editor update jobs for '{}'", specificInput);
            for(Job job : existingJobs) {
                job.cancel();
            }
        }
    }
}
