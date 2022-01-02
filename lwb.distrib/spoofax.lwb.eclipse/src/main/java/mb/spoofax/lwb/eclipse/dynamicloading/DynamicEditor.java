package mb.spoofax.lwb.eclipse.dynamicloading;

import mb.spoofax.eclipse.EclipseLanguageComponent;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.spoofax.eclipse.editor.SpoofaxEditorBase;
import mb.spoofax.eclipse.editor.SpoofaxSourceViewerConfiguration;
import mb.spoofax.eclipse.util.StyleUtil;
import mb.spoofax.lwb.dynamicloading.DynamicLanguage;
import mb.spoofax.lwb.dynamicloading.DynamicLanguageRegistry;
import mb.spoofax.lwb.eclipse.SpoofaxLwbLifecycleParticipant;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;

public class DynamicEditor extends SpoofaxEditorBase {
    public static final String id = "spoofax.lwb.eclipse.dynamicloading.editor";

    /*
    Do NOT initialize any of the following fields to null, as TextEditor's constructor will call 'initializeEditor' to
    initialize several fields, which will then be set back to null when initialized here.
    */

    // Set in initializeEditor, never null after that.
    protected @MonotonicNonNull StyleUtil styleUtil;
    protected @MonotonicNonNull DynamicLanguageRegistry languageRegistry;

    // Set in createSourceViewer/setInput, unset in dispose, may be null when no file is set.
    protected @Nullable String languageId;


    public @Nullable String getLanguageId() {
        return languageId;
    }


    public boolean enabled() {
        return documentListener != null;
    }

    public void enableOrUpdate() {
        if(!isInitialized() || enabled()) {
            scheduleJob(true);
            return;
        }
        logger.debug("Enabling editor for {}", inputName);
        documentListener = new DocumentListener();
        document.addDocumentListener(documentListener);
        scheduleJob(true);
    }

    public void disable() {
        if(!isInitialized() || !enabled()) {
            return;
        }
        logger.debug("Disabling editor for {}", inputName);
        document.removeDocumentListener(documentListener);
        documentListener = null;

        final TextPresentation defaultPresentation = styleUtil.createDefaultTextPresentation(document.getLength());
        presentationMerger.invalidate();
        Display.getDefault().asyncExec(() -> sourceViewer.changeTextPresentation(defaultPresentation, true));
    }

    @Override public void reconfigure() {
        setLanguageId();
        super.reconfigure();
        scheduleJob(true);
    }

    @Override protected void initializeEditor() {
        super.initializeEditor();

        this.styleUtil = SpoofaxPlugin.getPlatformComponent().getStyleUtil();
        this.languageRegistry = SpoofaxLwbLifecycleParticipant.getInstance().getDynamicLoadingComponent().getDynamicLanguageRegistry();
    }

    @Override protected void setInput() {
        super.setInput();
        setLanguageId();
    }

    @Override protected SourceViewerConfiguration createSourceViewerConfiguration() {
        if(languageId != null) {
            final @Nullable DynamicLanguage language = languageRegistry.getLanguageForId(languageId);
            if(language != null) {
                return new SpoofaxSourceViewerConfiguration(this, language.getLanguageComponent(), language.getPieComponent());
            }
        }
        return new SpoofaxSourceViewerConfiguration(this);
    }

    @Override protected void configureSourceViewerDecorationSupport(SourceViewerDecorationSupport support) {
        super.configureSourceViewerDecorationSupport(support);
        if(languageId != null) {
            final @Nullable DynamicLanguage language = languageRegistry.getLanguageForId(languageId);
            if(language != null) {
                setBracketSymbols(language.getLanguageComponent().getLanguageInstance(), support);
            }
        }
    }

    @Override
    protected ISourceViewer createSourceViewer(@NonNull Composite parent, @NonNull IVerticalRuler ruler, int styles) {
        final ISourceViewer sourceViewer = super.createSourceViewer(parent, ruler, styles);
        reconfigure();
        return sourceViewer;
    }

    private void setLanguageId() {
        final @Nullable String fileExtension = getFileExtension();
        if(fileExtension == null) {
            logger.error("Cannot set dynamically loaded language for editor '{}' because its input does not have have a file extension", inputName);
            return;
        }
        final @Nullable DynamicLanguage language = languageRegistry.getLanguageForFileExtension(fileExtension);
        if(language == null) {
            logger.error("Cannot set dynamically loaded language for editor '{}' because no language was found for file extension '{}'", inputName, fileExtension);
            return;
        }
        languageId = language.getId();
        logger.debug("Set dynamically loaded language for editor '{}' to '{}'", inputName, languageId);
    }

    @Override protected void scheduleJob(boolean initialUpdate) {
        // TODO: support case where file is null but document is not.
        if(input == null || document == null || file == null || languageId == null) return;
        final @Nullable EclipseDynamicLanguage language = (EclipseDynamicLanguage)languageRegistry.getLanguageForId(languageId);
        if(language == null) {
            logger.error("Cannot schedule editor update job for editor '{}' because no language for id '{}' was found", inputName, languageId);
            return;
        }
        final EclipseLanguageComponent languageComponent = language.getLanguageComponent();
        logger.debug("Scheduling update job for editor '{}' of dynamically loaded language '{}'", inputName, language);

        cancelJobs();
        final Job job = languageComponent.editorUpdateJobFactory().create(languageComponent, language.getPieComponent(), project, file, document, input, this);

        //A dd refresh scheduling rule because listing/walking a resource may require refreshes.
        final @Nullable ISchedulingRule refreshSchedulingRule;
        if(project != null) {
            refreshSchedulingRule = ResourcesPlugin.getWorkspace().getRuleFactory().refreshRule(project);
        } else {
            refreshSchedulingRule = null;
        }

        // noinspection ConstantConditions (scheduling rules may be null)
        job.setRule(MultiRule.combine(new ISchedulingRule[]{
            refreshSchedulingRule,
            file,
            languageComponent.startupReadLockRule()
        }));
        job.schedule(initialUpdate ? 0 : 300);
    }
}
