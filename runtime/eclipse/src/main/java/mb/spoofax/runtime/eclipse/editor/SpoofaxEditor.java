package mb.spoofax.runtime.eclipse.editor;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
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

import com.google.inject.Injector;

import mb.log.Logger;
import mb.spoofax.runtime.eclipse.SpoofaxPlugin;
import mb.spoofax.runtime.eclipse.pipeline.PipelineAdapter;
import mb.spoofax.runtime.eclipse.util.Nullable;
import mb.spoofax.runtime.eclipse.vfs.EclipsePathSrv;
import mb.spoofax.runtime.model.SpoofaxFacade;
import mb.vfs.path.PPath;

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
    private EclipsePathSrv pathSrv;
    private PipelineAdapter pipelineAdapter;

    private IEditorInput input;
    private String inputName;
    private IDocument document;
    private PPath file;
    private PPath projectPath;
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

    public PPath file() {
        return file;
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

        this.logger = spoofaxFacade.rootLogger;
        this.pathSrv = injector.getInstance(EclipsePathSrv.class);
        this.pipelineAdapter = injector.getInstance(PipelineAdapter.class);

        setDocumentProvider(new DocumentProvider(logger, pathSrv));
        setSourceViewerConfiguration(new SpoofaxSourceViewerConfiguration());
    }

    @Override protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
        input = getEditorInput();
        document = getDocumentProvider().getDocument(input);

        final PPath resource = pathSrv.resolve(input);
        if(resource != null) {
            inputName = resource.toString();
            file = resource;
            final IResource eclipseFile = pathSrv.unresolve(resource);
            if(eclipseFile != null) {
                final IProject project = eclipseFile.getProject();
                projectPath = pathSrv.resolve(project);
            }
        } else {
            inputName = input.getName();
            file = null;
            projectPath = null;
            logger.warn("File for editor on {} is null, cannot update the editor", input);
        }

        documentListener = new DocumentListener();
        document.addDocumentListener(documentListener);

        sourceViewer = super.createSourceViewer(parent, ruler, styles);
        final SourceViewerDecorationSupport decorationSupport = getSourceViewerDecorationSupport(sourceViewer);
        configureSourceViewerDecorationSupport(decorationSupport);

        ((ITextViewerExtension4) sourceViewer).addTextPresentationListener(presentationMerger);

        if(file != null && projectPath == null) {
            pipelineAdapter.addEditor(this, text(), file, projectPath);
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
        if(file == null || projectPath == null) {
            return;
        }
        final Job job = new EditorUpdateJob(logger, pipelineAdapter, this, document.get(), file, projectPath, input);
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
