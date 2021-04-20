package mb.spoofax.lwb.eclipse.dynamicloading;

import mb.spoofax.eclipse.EclipseLanguageComponent;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.spoofax.eclipse.editor.SpoofaxEditorBase;
import mb.spoofax.eclipse.util.StyleUtil;
import mb.spoofax.lwb.dynamicloading.DynamicLanguage;
import mb.spoofax.lwb.dynamicloading.DynamicLanguageRegistry;
import mb.spoofax.lwb.eclipse.SpoofaxLwbLifecycleParticipant;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.swt.widgets.Display;

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
        super.reconfigure();
        setLanguageId();
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

        job.setRule(MultiRule.combine(new ISchedulingRule[]{
            file, // May be null, but hat is a valid scheduling rule
            languageComponent.startupReadLockRule()
        }));
        job.schedule(initialUpdate ? 0 : 300);
    }
}
