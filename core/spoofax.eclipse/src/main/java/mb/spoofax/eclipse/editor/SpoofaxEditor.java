package mb.spoofax.eclipse.editor;

import mb.common.region.Region;
import mb.common.region.Selection;
import mb.common.region.Selections;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.spoofax.eclipse.EclipseLanguageComponent;
import mb.spoofax.eclipse.EclipsePlatformComponent;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.spoofax.eclipse.pie.PieRunner;
import mb.spoofax.eclipse.util.EditorInputUtil;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewerExtension4;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;

import java.util.Objects;
import java.util.Optional;

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

    private final EclipseLanguageComponent languageComponent;

    /*
    Do NOT initialize any of the following fields to null, as TextEditor's constructor will call 'initializeEditor' to
    initialize several fields, which will then be set back to null when initialized here.
    */

    // Set in initializeEditor, never null after that.
    private @MonotonicNonNull IJobManager jobManager;
    private @MonotonicNonNull LoggerFactory loggerFactory;
    private @MonotonicNonNull Logger logger;
    private @MonotonicNonNull PieRunner pieRunner;

    // Set in createSourceViewer, unset in dispose, may never be null otherwise.
    private @Nullable IEditorInput input;
    private @Nullable ISourceViewer sourceViewer;

    // Set in createSourceViewer, if unset in dispose, may never be null if documentProvider returns a null document.
    private @Nullable IDocument document;
    private @Nullable DocumentListener documentListener;
    private @Nullable IProject project;
    private @Nullable IFile file;


    protected SpoofaxEditor(EclipseLanguageComponent languageComponent) {
        super();
        this.languageComponent = languageComponent;
    }


    public EclipseLanguageComponent getLanguageComponent() {
        return languageComponent;
    }

    public @Nullable IProject getProject() {
        return project;
    }

    public @Nullable IFile getFile() {
        return file;
    }

    public Optional<Selection> getSelection() {
        final @Nullable ISelection selection = doGetSelection();
        if(selection instanceof ITextSelection) {
            final ITextSelection s = (ITextSelection)selection;
            final int length = s.getLength();
            if(length == 0) {
                return Optional.of(Selections.offset(s.getOffset()));
            }
            return Optional.of(Selections.region(Region.fromOffsetLength(s.getOffset(), length)));
        }
        return Optional.empty();
    }


    public void setStyleAsync(TextPresentation textPresentation, @Nullable String text, int textLength, @Nullable IProgressMonitor monitor) {
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
            // Cancel if editor has no text.
            final String currentText = document.get();
            if(currentText == null) {
                return;
            }
            // Cancel if the text the presentation was made for is different than the current text.
            if(textLength != currentText.length() || (text != null && !text.equals(currentText))) {
                return;
            }
            try {
                sourceViewer.changeTextPresentation(textPresentation, true);
            } catch(IllegalArgumentException e) {
                logger.error("Changing text presentation asynchronously failed unexpectedly", e);
            }
        });
    }


    @Override protected void initializeEditor() {
        super.initializeEditor();

        this.jobManager = Job.getJobManager();

        final EclipsePlatformComponent component = SpoofaxPlugin.getPlatformComponent();
        this.loggerFactory = component.getLoggerFactory();
        this.logger = loggerFactory.create(getClass());
        this.pieRunner = component.getPieRunner();

        setDocumentProvider(new SpoofaxDocumentProvider());
        setSourceViewerConfiguration(new SpoofaxSourceViewerConfiguration());
        setEditorContextMenuId("#SpoofaxEditorContext");
    }

    @Override
    protected ISourceViewer createSourceViewer(@NonNull Composite parent, @NonNull IVerticalRuler ruler, int styles) {
        input = getEditorInput();
        Objects.requireNonNull(input); // Hint to editor that input cannot be null.

        final IDocumentProvider documentProvider = getDocumentProvider();
        document = documentProvider.getDocument(input);
        if(document != null) {
            documentListener = new DocumentListener();
            document.addDocumentListener(documentListener);

            final @Nullable IFile file = EditorInputUtil.getFile(input);
            if(file != null) {
                final @Nullable IProject eclipseProject = file.getProject();
                if(eclipseProject != null) {
                    project = eclipseProject;
                }
                this.file = file;
            }
        } else {
            logger.error("Editor cannot be initialized, document provider '{}' returned null for editor input '{}'", documentProvider, input);
            file = null;
            documentListener = null;
        }

        sourceViewer = super.createSourceViewer(parent, ruler, styles);
        Objects.requireNonNull(sourceViewer); // Hint to editor that sourceViewer cannot be null.
        final SourceViewerDecorationSupport decorationSupport = getSourceViewerDecorationSupport(sourceViewer);
        configureSourceViewerDecorationSupport(decorationSupport);

        ((ITextViewerExtension4)sourceViewer).addTextPresentationListener(presentationMerger);

        if(document != null) {
            scheduleJob(true);
        }

        return sourceViewer;
    }

    @Override public void dispose() {
        cancelJobs();

        if(document != null && documentListener != null) {
            document.removeDocumentListener(documentListener);
        }

        if(file != null) {
            pieRunner.removeEditor(file);
        }

        input = null;
        sourceViewer = null;

        document = null;
        documentListener = null;
        file = null;

        super.dispose();
    }


    private void scheduleJob(boolean initialUpdate) {
        if(document == null || file == null) return; // TODO: support case where file is null but document is not.
        cancelJobs();
        final Job job = new EditorUpdateJob(loggerFactory, pieRunner, languageComponent, project, file, document, this);
        job.setRule(MultiRule.combine(file /* May return null, but null is a valid scheduling rule */, languageComponent.startupReadLockRule()));
        job.schedule(initialUpdate ? 0 : 300);
    }

    private void cancelJobs() {
        final Job[] existingJobs = jobManager.find(this);
        if(existingJobs.length > 0) {
            logger.trace("Cancelling editor update jobs for '{}'", this);
            for(Job job : existingJobs) {
                job.cancel();
            }
        }
    }
}
