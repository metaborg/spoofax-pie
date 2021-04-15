package mb.spoofax.lwb.eclipse.dynamicloading;

import mb.spoofax.eclipse.EclipseLanguageComponent;
import mb.spoofax.eclipse.editor.SpoofaxEditorBase;
import mb.spoofax.lwb.dynamicloading.DynamicLanguage;
import mb.spoofax.lwb.dynamicloading.DynamicLanguageRegistry;
import mb.spoofax.lwb.eclipse.SpoofaxLwbLifecycleParticipant;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.MultiRule;

public class DynamicEditor extends SpoofaxEditorBase {
    public static final String id = "spoofax.lwb.eclipse.dynamicloading.editor";

    /*
    Do NOT initialize any of the following fields to null, as TextEditor's constructor will call 'initializeEditor' to
    initialize several fields, which will then be set back to null when initialized here.
    */

    // Set in initializeEditor, never null after that.
    protected @MonotonicNonNull DynamicLanguageRegistry languageRegistry;


    @Override protected void initializeEditor() {
        super.initializeEditor();

        this.languageRegistry = SpoofaxLwbLifecycleParticipant.getInstance().getDynamicLoadingComponent().getDynamicLanguageRegistry();
    }

    @Override protected void scheduleJob(boolean initialUpdate) {
        if(document == null || file == null) return; // TODO: support case where file is null but document is not.
        final @Nullable String fileExtension = file.getFileExtension();
        if(fileExtension == null) return;
        final @Nullable DynamicLanguage language = languageRegistry.getLanguageForFileExtension(fileExtension);
        if(language == null) return;
        if(!(language.getLanguageComponent() instanceof EclipseLanguageComponent)) {
            logger.error("Cannot schedule editor update job for '{}' because its language component is not of type EclipseLanguageComponent", language);
            return;
        }
        final EclipseLanguageComponent languageComponent = (EclipseLanguageComponent)language.getLanguageComponent();

        cancelJobs();
        final Job job = languageComponent.editorUpdateJobFactory().create(languageComponent, language.getPieComponent(), project, file, document, this);

        // TODO: share following code with SpoofaxEditor?

        // HACK: try to pass the build directory as a scheduling rule, because sometimes an editor update may require
        //       unarchiving files into the build directory (usually for meta-languages). This is fine, but the build
        //       directory should not be hard coded!
        final @Nullable IFolder buildDirectory;
        if(project != null) {
            buildDirectory = project.getFolder("build");
        } else {
            buildDirectory = null;
        }

        //noinspection ConstantConditions
        job.setRule(MultiRule.combine(new ISchedulingRule[]{
            buildDirectory, // May be null, but hat is a valid scheduling rule
            file, // May be null, but hat is a valid scheduling rule
            languageComponent.startupReadLockRule()
        }));
        job.schedule(initialUpdate ? 0 : 300);
    }
}
