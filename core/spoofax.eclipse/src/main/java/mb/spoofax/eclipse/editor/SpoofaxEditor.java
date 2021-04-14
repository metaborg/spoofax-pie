package mb.spoofax.eclipse.editor;

import mb.common.region.Region;
import mb.common.region.Selection;
import mb.common.region.Selections;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.pie.dagger.PieComponent;
import mb.spoofax.eclipse.EclipseLanguageComponent;
import mb.spoofax.eclipse.EclipsePlatformComponent;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.spoofax.eclipse.log.EclipseLoggerComponent;
import mb.spoofax.eclipse.pie.PieRunner;
import mb.spoofax.eclipse.util.EditorInputUtil;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
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

public abstract class SpoofaxEditor extends SpoofaxEditorBase {
    private final EclipseLanguageComponent languageComponent;
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
        this.pieComponent = pieComponent;
    }


    public EclipseLanguageComponent getLanguageComponent() {
        return languageComponent;
    }


    @Override protected void scheduleJob(boolean initialUpdate) {
        if(document == null || file == null) return; // TODO: support case where file is null but document is not.
        cancelJobs();
        final Job job = new EditorUpdateJob(loggerFactory, pieRunner, languageComponent, pieComponent, project, file, document, this);

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

    @Override protected void initializeEditor() {
        super.initializeEditor();

        final EclipsePlatformComponent platformComponent = SpoofaxPlugin.getPlatformComponent();
        this.pieRunner = platformComponent.getPieRunner();
    }

    @Override public void dispose() {
        super.dispose();

        if(file != null) {
            pieRunner.removeEditor(file);
        }
    }
}
