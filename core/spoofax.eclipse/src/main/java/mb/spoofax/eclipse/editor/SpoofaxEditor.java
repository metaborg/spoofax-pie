package mb.spoofax.eclipse.editor;

import mb.pie.dagger.PieComponent;
import mb.spoofax.eclipse.EclipseLanguageComponent;
import mb.spoofax.eclipse.EclipsePlatformComponent;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.spoofax.eclipse.pie.PieRunner;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;

public abstract class SpoofaxEditor extends SpoofaxEditorBase {
    private final EclipseLanguageComponent languageComponent;
    private final EditorUpdateJob.Factory editorUpdateJobFactory;
    private final PieComponent pieComponent;

    /*
    Do NOT initialize any of the following fields to null, as TextEditor's constructor will call 'initializeEditor' to
    initialize several fields, which will then be set back to null when initialized here.
    */

    // Set in initializeEditor, never null after that.
    private @MonotonicNonNull PieRunner pieRunner;


    protected SpoofaxEditor(EclipseLanguageComponent languageComponent, PieComponent pieComponent) {
        super();
        this.languageComponent = languageComponent;
        this.editorUpdateJobFactory = languageComponent.editorUpdateJobFactory();
        this.pieComponent = pieComponent;
    }


    public EclipseLanguageComponent getLanguageComponent() {
        return languageComponent;
    }


    @Override protected void scheduleJob(boolean initialUpdate) {
        // TODO: support case where file is null but document is not.
        if(input == null || document == null || file == null) return;
        cancelJobs();

        final EditorUpdateJob job = editorUpdateJobFactory.create(languageComponent, pieComponent, project, file, document, input, this);

        // HACK: try to pass the build directory as a scheduling rule, because sometimes an editor update may require
        //       unarchiving files into the build directory (usually for meta-languages). This is fine, but the build
        //       directory should not be hard coded!
        final @Nullable IFolder buildDirectory;
        if(project != null) {
            final IFolder folder = project.getFolder("build");
            if(folder.exists()) {
                buildDirectory = folder;
            } else {
                buildDirectory = null;
            }
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

    @Override protected void initializeEditor() {
        super.initializeEditor();

        final EclipsePlatformComponent platformComponent = SpoofaxPlugin.getPlatformComponent();
        this.pieRunner = platformComponent.getPieRunner();
    }

    @Override public void dispose() {
        if(file != null) {
            pieRunner.removeEditor(languageComponent, pieComponent, project, file);
        }
        super.dispose();
    }
}
