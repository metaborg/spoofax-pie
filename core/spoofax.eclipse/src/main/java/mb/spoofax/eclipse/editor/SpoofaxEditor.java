package mb.spoofax.eclipse.editor;

import mb.pie.dagger.PieComponent;
import mb.spoofax.eclipse.EclipseLanguageComponent;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
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
        final ISchedulingRule rule = getJobSchedulingRule();
        job.setRule(rule);
        job.schedule(initialUpdate ? 0 : 300);
    }

    @Override public void dispose() {
        if(file != null) {
            final EditorCloseJob job = editorCloseJobFactory.create(languageComponent, pieComponent, project, file);
            final ISchedulingRule rule = getJobSchedulingRule();
            job.setRule(rule);
            job.schedule();
        }
        super.dispose();
    }


    private ISchedulingRule getJobSchedulingRule() {
        return getJobSchedulingRule(languageComponent, true);
    }
}
