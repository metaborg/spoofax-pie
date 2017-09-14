package mb.spoofax.runtime.eclipse.editor;

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

import mb.log.Logger;
import mb.pie.runtime.core.BuildManager;
import mb.spoofax.runtime.eclipse.SpoofaxPlugin;
import mb.spoofax.runtime.eclipse.build.Updater;
import mb.spoofax.runtime.eclipse.vfs.EclipsePathSrv;
import mb.spoofax.runtime.model.SpoofaxFacade;
import mb.spoofax.runtime.model.context.Context;
import mb.spoofax.runtime.model.context.ContextFactory;
import mb.spoofax.runtime.pie.PieSrv;
import mb.vfs.path.PPath;

public class SpoofaxEditor extends TextEditor {
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
    private PieSrv pieSrv;
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

        final SpoofaxFacade spoofaxFacade = SpoofaxPlugin.spoofaxFacade();
        final Injector injector = spoofaxFacade.injector;

        this.logger = spoofaxFacade.rootLogger;
        this.pathSrv = injector.getInstance(EclipsePathSrv.class);
        this.contextFactory = injector.getInstance(ContextFactory.class);
        this.pieSrv = injector.getInstance(PieSrv.class);
        this.editors = injector.getInstance(Editors.class);
        this.updater = injector.getInstance(Updater.class);

        this.workspaceRoot = pathSrv.resolveWorkspaceRoot();

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
        final BuildManager buildManager = pieSrv.get(workspaceRoot);
        final Job job = new EditorUpdateJob(logger, buildManager, updater, this, document.get(), context, input, file,
            eclipseFile, workspaceRoot);
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
