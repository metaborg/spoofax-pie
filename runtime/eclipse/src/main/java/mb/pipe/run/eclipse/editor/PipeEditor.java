package mb.pipe.run.eclipse.editor;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.TextEditor;

import com.google.inject.Injector;

import mb.pipe.run.core.PipeFacade;
import mb.pipe.run.core.log.ILogger;
import mb.pipe.run.core.model.Context;
import mb.pipe.run.core.model.IContext;
import mb.pipe.run.core.vfs.IResource;
import mb.pipe.run.eclipse.PipePlugin;
import mb.pipe.run.eclipse.vfs.IEclipseResourceSrv;

public class PipeEditor extends TextEditor {
    private final class DocumentListener implements IDocumentListener {
        @Override public void documentAboutToBeChanged(DocumentEvent event) {

        }

        @Override public void documentChanged(DocumentEvent event) {
            scheduleJob(false);
        }
    }

    private IJobManager jobManager;

    private ILogger logger;
    private IEclipseResourceSrv resourceSrv;

    private DocumentListener documentListener;

    private IEditorInput input;
    private IDocument document;
    private org.eclipse.core.resources.IResource eclipseResource;


    @Override protected void initializeEditor() {
        super.initializeEditor();

        this.jobManager = Job.getJobManager();

        final PipeFacade pipeFacade = PipePlugin.pipeFacade();
        final Injector injector = pipeFacade.injector;

        this.logger = pipeFacade.rootLogger;
        this.resourceSrv = injector.getInstance(IEclipseResourceSrv.class);

        setDocumentProvider(new DocumentProvider(logger, resourceSrv));
        setSourceViewerConfiguration(new PipeSourceViewerConfiguration());
    }

    @Override protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
        input = getEditorInput();
        document = getDocumentProvider().getDocument(input);

        final IResource resource = resourceSrv.resolve(input);
        if(resource != null) {
            eclipseResource = resourceSrv.unresolve(resource);
        } else {
            logger.warn("Resource for editor on {} is null, cannot update the editor", input);
        }

        documentListener = new DocumentListener();
        document.addDocumentListener(documentListener);

        final ISourceViewer sourceViewer = super.createSourceViewer(parent, ruler, styles);

        scheduleJob(true);

        return sourceViewer;
    }

    @Override public void dispose() {
        cancelJobs(input);

        if(documentListener != null) {
            document.removeDocumentListener(documentListener);
        }

        input = null;
        document = null;
        documentListener = null;

        super.dispose();
    }

    private void scheduleJob(boolean instantaneous) {
        cancelJobs(input);
        final IProject project = eclipseResource.getProject();
        final IResource projectDir = resourceSrv.resolve(project);
        final IContext context = new Context(projectDir);
        final Job job = new EditorUpdateJob(logger, document.get(), context, input, eclipseResource);
        job.setRule(eclipseResource);
        job.schedule(instantaneous ? 0 : 300);
    }

    private void cancelJobs(IEditorInput specificInput) {
        logger.trace("Cancelling editor update jobs for {}", specificInput);
        final Job[] existingJobs = jobManager.find(specificInput);
        for(Job job : existingJobs) {
            job.cancel();
        }
    }
}
