package mb.spoofax.lwb.eclipse.dynamicloading;

import mb.spoofax.core.Coordinate;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.eclipse.EclipseLanguageComponent;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.spoofax.eclipse.editor.SpoofaxEditorBase;
import mb.spoofax.eclipse.editor.SpoofaxSourceViewerConfiguration;
import mb.spoofax.eclipse.util.StyleUtil;
import mb.spoofax.lwb.dynamicloading.component.DynamicComponent;
import mb.spoofax.lwb.dynamicloading.component.DynamicComponentManager;
import mb.spoofax.lwb.eclipse.SpoofaxLwbPlugin;
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
    protected @MonotonicNonNull DynamicComponentManager dynamicComponentManager;

    // Set in createSourceViewer/setInput, unset in dispose, may be null when no file is set.
    protected @Nullable Coordinate componentCoordinate;


    public @Nullable Coordinate getComponentCoordinate() {
        return componentCoordinate;
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
        this.dynamicComponentManager = SpoofaxLwbPlugin.getDynamicLoadingComponent().getDynamicComponentManager();
    }

    @Override protected void setInput() {
        super.setInput();
        setLanguageId();
    }

    @Override protected SourceViewerConfiguration createSourceViewerConfiguration() {
        if(componentCoordinate != null) {
            final @Nullable DynamicComponent component = dynamicComponentManager.getDynamicComponent(componentCoordinate).get();
            if(component != null) {
                return new SpoofaxSourceViewerConfiguration(this, component.getLanguageComponent().get(), component.getPieComponent());
            }
        }
        return new SpoofaxSourceViewerConfiguration(this);
    }

    @Override protected void configureSourceViewerDecorationSupport(SourceViewerDecorationSupport support) {
        super.configureSourceViewerDecorationSupport(support);
        if(componentCoordinate != null) {
            final @Nullable DynamicComponent language = dynamicComponentManager.getDynamicComponent(componentCoordinate).get();
            if(language != null) {
                final @Nullable LanguageComponent languageComponent = language.getLanguageComponent().get();
                if(languageComponent != null) {
                    setBracketSymbols(languageComponent.getLanguageInstance(), support);
                }
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
        final @Nullable DynamicComponent language = dynamicComponentManager.getDynamicComponent(fileExtension).get();
        if(language == null) {
            logger.error("Cannot set dynamically loaded language for editor '{}' because no language was found for file extension '{}'", inputName, fileExtension);
            return;
        }
        componentCoordinate = language.getCoordinate();
        logger.debug("Set dynamically loaded language for editor '{}' to '{}'", inputName, componentCoordinate);
    }

    @Override protected void scheduleJob(boolean initialUpdate) {
        // TODO: support case where file is null but document is not.
        if(input == null || document == null || file == null || componentCoordinate == null) return;
        final @Nullable DynamicComponent component = dynamicComponentManager.getDynamicComponent(componentCoordinate).get();
        if(component == null) {
            logger.error("Cannot schedule editor update job for editor '{}' because no component for coordinate '{}' was found", inputName, componentCoordinate);
            return;
        }
        final @Nullable LanguageComponent languageComponent = component.getLanguageComponent().get();
        if(languageComponent == null) {
            return; // Component has no language component.
        }
        if(!(languageComponent instanceof EclipseLanguageComponent)) {
            logger.error("Cannot schedule editor update job for editor '{}' because component for coordinate '{}' does not have a language component that implements EclipseLanguageComponent", inputName, componentCoordinate);
            return;
        }
        final EclipseLanguageComponent eclipseLanguageComponent = (EclipseLanguageComponent)languageComponent;
        logger.debug("Scheduling update job for editor '{}' of dynamically loaded language '{}'", inputName, component);

        cancelJobs();
        final Job job = eclipseLanguageComponent.editorUpdateJobFactory().create(eclipseLanguageComponent, component.getPieComponent(), project, file, document, input, this);
        final ISchedulingRule rule = getJobSchedulingRule(eclipseLanguageComponent);
        job.setRule(rule);
        job.schedule(initialUpdate ? 0 : 300);
    }

    private ISchedulingRule getJobSchedulingRule(EclipseLanguageComponent languageComponent) {
        return getJobSchedulingRule(languageComponent, false);
    }
}
