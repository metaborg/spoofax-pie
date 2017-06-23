package mb.pipe.run.eclipse.editor;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
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
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;

import com.google.inject.Injector;

import mb.ceres.BuildManager;
import mb.pipe.run.ceres.CeresSrv;
import mb.pipe.run.core.PipeFacade;
import mb.pipe.run.core.log.Logger;
import mb.pipe.run.core.model.Context;
import mb.pipe.run.core.model.ContextFactory;
import mb.pipe.run.core.path.PPath;
import mb.pipe.run.eclipse.PipePlugin;
import mb.pipe.run.eclipse.build.Updater;
import mb.pipe.run.eclipse.vfs.EclipsePathSrv;

public class PipeEditor extends TextEditor {
    private final class DocumentListener implements IDocumentListener {
        @Override public void documentAboutToBeChanged(DocumentEvent event) {

        }

        @Override public void documentChanged(DocumentEvent event) {
            scheduleJob(false);
        }
    }

    private IJobManager jobManager;
    private Logger logger;
    private EclipsePathSrv pathSrv;
    private ContextFactory contextFactory;
    private CeresSrv ceresSrv;
    private Editors editors;
    private Updater updater;

    private IEditorInput input;
    private String inputName;
    private IDocument document;
    private PPath file;
    private IResource eclipseFile;
    private PPath workspaceRoot;
    private DocumentListener documentListener;
    private ISourceViewer sourceViewer;


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

    public IResource eclipseFile() {
        return eclipseFile;
    }


    @Override protected void initializeEditor() {
        super.initializeEditor();

        this.jobManager = Job.getJobManager();

        final PipeFacade pipeFacade = PipePlugin.pipeFacade();
        final Injector injector = pipeFacade.injector;

        this.logger = pipeFacade.rootLogger;
        this.pathSrv = injector.getInstance(EclipsePathSrv.class);
        this.contextFactory = injector.getInstance(ContextFactory.class);
        this.ceresSrv = injector.getInstance(CeresSrv.class);
        this.editors = injector.getInstance(Editors.class);
        this.updater = injector.getInstance(Updater.class);

        this.workspaceRoot = pathSrv.resolveWorkspaceRoot();

        setDocumentProvider(new DocumentProvider(logger, pathSrv));
        setSourceViewerConfiguration(new PipeSourceViewerConfiguration());
    }

    @Override protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
        input = getEditorInput();
        document = getDocumentProvider().getDocument(input);

        final PPath resource = pathSrv.resolve(input);
        if(resource != null) {
            inputName = resource.toString();
            file = resource;
            eclipseFile = pathSrv.unresolve(resource);
        } else {
            inputName = input.getName();
            file = null;
            eclipseFile = null;
            logger.warn("File for editor on {} is null, cannot update the editor", input);
        }

        documentListener = new DocumentListener();
        document.addDocumentListener(documentListener);

        sourceViewer = super.createSourceViewer(parent, ruler, styles);
        final SourceViewerDecorationSupport decorationSupport = getSourceViewerDecorationSupport(sourceViewer);
        configureSourceViewerDecorationSupport(decorationSupport);

        editors.addEditor(this);

        scheduleJob(true);

        return sourceViewer;
    }

    @Override public void dispose() {
        cancelJobs(input);

        if(documentListener != null) {
            document.removeDocumentListener(documentListener);
        }

        editors.removeEditor(this);

        input = null;
        document = null;
        documentListener = null;

        super.dispose();
    }

    private void scheduleJob(boolean instantaneous) {
        cancelJobs(input);
        if(eclipseFile == null) {
            return;
        }

        final IProject project = eclipseFile.getProject();
        final PPath projectDir = pathSrv.resolve(project);
        final Context context = contextFactory.create(projectDir);
        final BuildManager buildManager = ceresSrv.get(context);
        final Job job = new EditorUpdateJob(logger, buildManager, updater, sourceViewer, document.get(), context, input,
            file, eclipseFile, workspaceRoot);
        job.setRule(eclipseFile);
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
