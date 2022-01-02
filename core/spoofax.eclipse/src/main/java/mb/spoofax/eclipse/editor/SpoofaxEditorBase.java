package mb.spoofax.eclipse.editor;

import mb.common.region.Region;
import mb.common.region.Selection;
import mb.common.region.Selections;
import mb.common.util.ListView;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.spoofax.common.BracketSymbols;
import mb.spoofax.core.language.LanguageInstance;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.spoofax.eclipse.log.EclipseLoggerComponent;
import mb.spoofax.eclipse.util.EditorInputUtil;
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
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewerExtension4;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.source.DefaultCharacterPairMatcher;
import org.eclipse.jface.text.source.ICharacterPairMatcher;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.ISourceViewerExtension2;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;

import java.util.Optional;

public abstract class SpoofaxEditorBase extends TextEditor {
    public final class DocumentListener implements IDocumentListener {
        @Override public void documentAboutToBeChanged(@NonNull DocumentEvent event) {
            // Don't care about this event.
        }

        @Override public void documentChanged(@NonNull DocumentEvent event) {
            scheduleJob(false);
        }
    }

    public final class EditorInputChangedListener implements IPropertyListener {
        @Override public void propertyChanged(Object source, int propId) {
            if(propId == IEditorPart.PROP_INPUT) {
                editorInputChanged();
            }
        }
    }


    protected final IPropertyListener editorInputChangedListener = new EditorInputChangedListener();
    protected final PresentationMerger presentationMerger = new PresentationMerger();

    /*
    Do NOT initialize any of the following fields to null, as TextEditor's constructor will call 'initializeEditor' to
    initialize several fields, which will then be set back to null when initialized here.
    */

    // Set in initializeEditor, never null after that.
    protected @MonotonicNonNull IJobManager jobManager;
    protected @MonotonicNonNull LoggerFactory loggerFactory;
    protected @MonotonicNonNull Logger logger;

    // Set in createSourceViewer, unset in dispose, may never be null otherwise.
    protected @Nullable IEditorInput input;
    protected @Nullable String inputName;
    protected @Nullable ISourceViewer sourceViewer;
    protected @Nullable ISourceViewerExtension2 sourceViewerExt2;
    protected @Nullable ITextViewerExtension4 textViewerExt4;

    // Set in createSourceViewer, if unset in dispose, may never be null if documentProvider returns a null document.
    protected @Nullable IDocument document;
    protected @Nullable DocumentListener documentListener;
    protected @Nullable IProject project;
    protected @Nullable IFile file;


    protected SpoofaxEditorBase() {
        super();
    }


    public boolean isInitialized() {
        if(input == null || sourceViewer == null) {
            logger.error("Attempted to use editor before it was initialized");
            return false;
        }
        return true;
    }

    public @Nullable IDocument getDocument() {
        return document;
    }

    public @Nullable IProject getProject() {
        return project;
    }

    public @Nullable IFile getFile() {
        return file;
    }

    public @Nullable String getFileExtension() {
        if(file == null) return null;
        return file.getFileExtension();
    }


    @Override public ISelectionProvider getSelectionProvider() {
        return super.getSelectionProvider();
    }

    public ITextOperationTarget getTextOperationTarget() {
        return (ITextOperationTarget)getAdapter(ITextOperationTarget.class);
    }

    public SourceViewerConfiguration getSourceViewerConfigurationReally() {
        return getSourceViewerConfiguration();
    }

    public ISourceViewer getSourceViewerReally() {
        return getSourceViewer();
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

    public void reconfigure() {
        if(!isInitialized()) return;
        logger.debug("Reconfiguring editor for {}", inputName);
        final Display display = Display.getDefault();
        display.asyncExec(() -> {
            sourceViewerExt2.unconfigure();
            setSourceViewerConfiguration(this.createSourceViewerConfiguration());
            sourceViewer.configure(getSourceViewerConfiguration());
            final SourceViewerDecorationSupport decorationSupport = getSourceViewerDecorationSupport(sourceViewer);
            configureSourceViewerDecorationSupport(decorationSupport);
            decorationSupport.uninstall();
            decorationSupport.install(getPreferenceStore());
        });
    }


    protected abstract void scheduleJob(boolean initialUpdate);

    protected abstract SourceViewerConfiguration createSourceViewerConfiguration();

    @Override
    protected void initializeEditor() {
        super.initializeEditor();

        SpoofaxEditorPreferences.setDefaults(getPreferenceStore());

        this.jobManager = Job.getJobManager();

        final EclipseLoggerComponent loggerComponent = SpoofaxPlugin.getLoggerComponent();
        this.loggerFactory = loggerComponent.getLoggerFactory();
        this.logger = loggerFactory.create(getClass());

        setDocumentProvider(new SpoofaxDocumentProvider());
        setEditorContextMenuId("#SpoofaxEditorContext");
        setSourceViewerConfiguration(this.createSourceViewerConfiguration());
    }

    protected void setInput() {
        input = getEditorInput();
        final IDocumentProvider documentProvider = getDocumentProvider();
        document = documentProvider.getDocument(input);
        if(document != null) {
            // Register for changes in the text, to schedule editor updates.
            documentListener = new DocumentListener();
            document.addDocumentListener(documentListener);

            final @Nullable IFile file = EditorInputUtil.getFile(input);
            if(file != null) {
                inputName = file.toString();
                final @Nullable IProject eclipseProject = file.getProject();
                if(eclipseProject != null) {
                    project = eclipseProject;
                }
                this.file = file;
            } else {
                inputName = input.getName();
            }
        } else {
            logger.error("Editor cannot be initialized, document provider '{}' returned null for editor input '{}'", documentProvider, input);
            inputName = input.getName();
            file = null;
            documentListener = null;
        }
        logger.debug("Editor input was set to '{}'", inputName);
    }

    @Override
    protected ISourceViewer createSourceViewer(@NonNull Composite parent, @NonNull IVerticalRuler ruler, int styles) {
        setInput();

        // Register for changes in the editor input, to handle renaming or moving of resources of open editors.
        this.addPropertyListener(editorInputChangedListener);

        // Create source viewer after input, document, resources, and language have been set.
        sourceViewer = super.createSourceViewer(parent, ruler, styles);
        sourceViewerExt2 = (ISourceViewerExtension2)sourceViewer;
        textViewerExt4 = (ITextViewerExtension4)sourceViewer;
        final SourceViewerDecorationSupport decorationSupport = getSourceViewerDecorationSupport(sourceViewer);
        configureSourceViewerDecorationSupport(decorationSupport);

        // Register for changes in text presentation, to merge our text presentation with presentations from other
        // sources, such as marker annotations.
        textViewerExt4.addTextPresentationListener(presentationMerger);

        if(document != null) {
            scheduleJob(true);
        }

        return sourceViewer;
    }

    private void editorInputChanged() {
        final IEditorInput oldInput = input;
        final IDocument oldDocument = document;
        logger.debug("Editor input changed from {} to {}", oldInput, input);
        // Unregister old document listener and register a new one, because the document will change.
        if(documentListener != null) {
            oldDocument.removeDocumentListener(documentListener);
        }
        // Set new inputs.
        setInput();
        // Reconfigure the editor because the language may have changed.
        reconfigure();
        // Cancel and schedule a new job.
        cancelJobs(oldInput);
        scheduleJob(true);
    }

    @Override
    public void dispose() {
        cancelJobs();

        if(document != null && documentListener != null) {
            document.removeDocumentListener(documentListener);
        }
        this.removePropertyListener(editorInputChangedListener);
        if(textViewerExt4 != null) {
            textViewerExt4.removeTextPresentationListener(presentationMerger);
        }

        input = null;
        sourceViewer = null;
        sourceViewerExt2 = null;
        textViewerExt4 = null;

        document = null;
        documentListener = null;
        file = null;

        super.dispose();
    }


    protected void cancelJobs(IEditorInput specificInput) {
        final Job[] existingJobs = jobManager.find(specificInput);
        if(existingJobs.length > 0) {
            logger.trace("Cancelling editor update jobs for '{}'", specificInput);
            for(Job job : existingJobs) {
                job.cancel();
            }
        }
    }

    protected void cancelJobs() {
        final Job[] existingJobs = jobManager.find(this);
        if(existingJobs.length > 0) {
            logger.trace("Cancelling editor update jobs for '{}'", this);
            for(Job job : existingJobs) {
                job.cancel();
            }
        }
    }


    protected void setBracketSymbols(LanguageInstance languageInstance, SourceViewerDecorationSupport support) {
        final ListView<BracketSymbols> allBracketSymbols = languageInstance.getBracketSymbols();
        final char[] pairMatcherChars = new char[allBracketSymbols.size() * 2];
        int i = 0;
        for(BracketSymbols bracketSymbols : allBracketSymbols) {
            pairMatcherChars[i++] = bracketSymbols.open;
            pairMatcherChars[i++] = bracketSymbols.close;
        }
        final ICharacterPairMatcher matcher = new DefaultCharacterPairMatcher(pairMatcherChars);
        support.setCharacterPairMatcher(matcher);
        SpoofaxEditorPreferences.setPairMatcherKeys(support);
    }
}
