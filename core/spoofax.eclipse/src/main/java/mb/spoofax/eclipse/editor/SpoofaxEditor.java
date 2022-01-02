package mb.spoofax.eclipse.editor;

import mb.common.util.ListView;
import mb.pie.dagger.PieComponent;
import mb.spoofax.common.BracketSymbols;
import mb.spoofax.eclipse.EclipseLanguageComponent;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.jface.text.source.DefaultCharacterPairMatcher;
import org.eclipse.jface.text.source.ICharacterPairMatcher;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;

public abstract class SpoofaxEditor extends SpoofaxEditorBase {
    private final EclipseLanguageComponent languageComponent;
    private final EditorUpdateJob.Factory editorUpdateJobFactory;
    private final EditorCloseJob.Factory editorCloseJobFactory;
    private final PieComponent pieComponent;

    /*
    Do NOT initialize any of the following fields (none at the moment) to null, as TextEditor's constructor will call
    'initializeEditor' to initialize several fields, which will then be set back to null when initialized here.
    */


    protected SpoofaxEditor(EclipseLanguageComponent languageComponent, PieComponent pieComponent) {
        super();
        this.languageComponent = languageComponent;
        this.editorUpdateJobFactory = languageComponent.editorUpdateJobFactory();
        this.editorCloseJobFactory = languageComponent.editorCloseJobFactory();
        this.pieComponent = pieComponent;
    }


    public EclipseLanguageComponent getLanguageComponent() {
        return languageComponent;
    }


    @Override protected void configureSourceViewerDecorationSupport(SourceViewerDecorationSupport support) {
        super.configureSourceViewerDecorationSupport(support);
        setBracketSymbols(languageComponent.getLanguageInstance(), support);
    }

    @Override protected void scheduleJob(boolean initialUpdate) {
        // TODO: support case where file is null but document is not.
        if(input == null || document == null || file == null) return;
        cancelJobs();

        final EditorUpdateJob job = editorUpdateJobFactory.create(languageComponent, pieComponent, project, file, document, input, this);
        job.setRule(getJobSchedulingRule());
        job.schedule(initialUpdate ? 0 : 300);
    }

    @Override public void dispose() {
        if(file != null) {
            final EditorCloseJob job = editorCloseJobFactory.create(languageComponent, pieComponent, project, file);
            job.setRule(getJobSchedulingRule());
            job.schedule();
        }
        super.dispose();
    }


    private ISchedulingRule getJobSchedulingRule() {
        // HACK: try to pass the build directory as a scheduling rule, because sometimes an editor update may require
        //       unarchiving files into the build directory (usually for meta-languages). This is fine, but the build
        //       directory should not be hard coded! Also add refresh scheduling rule because listing/walking a resource
        //       may require refreshes.
        final @Nullable ISchedulingRule refreshSchedulingRule;
        final @Nullable ISchedulingRule buildDirectorySchedulingRule;
        if(project != null) {
            refreshSchedulingRule = ResourcesPlugin.getWorkspace().getRuleFactory().refreshRule(project);
            final IFolder folder = project.getFolder("build");
            if(folder.exists()) {
                buildDirectorySchedulingRule = folder;
            } else {
                buildDirectorySchedulingRule = null;
            }
        } else {
            buildDirectorySchedulingRule = null;
            refreshSchedulingRule = null;
        }
        return MultiRule.combine(new ISchedulingRule[]{
            refreshSchedulingRule,
            buildDirectorySchedulingRule,
            file,
            languageComponent.startupReadLockRule()
        });
    }
}
