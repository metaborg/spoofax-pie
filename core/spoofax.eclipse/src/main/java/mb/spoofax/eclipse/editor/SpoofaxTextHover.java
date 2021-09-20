package mb.spoofax.eclipse.editor;

import mb.common.editor.HoverResult;
import mb.common.option.Option;
import mb.pie.api.ExecException;
import mb.pie.api.Interactivity;
import mb.pie.api.MixedSession;
import mb.pie.api.TopDownSession;
import mb.pie.api.UncheckedExecException;
import mb.pie.dagger.PieComponent;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.eclipse.resource.EclipseResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.jface.text.DefaultTextHover;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.ISourceViewer;

import java.util.Collections;

public class SpoofaxTextHover extends DefaultTextHover {
    private final SpoofaxEditorBase editorBase;
    private final @Nullable LanguageComponent languageComponent;
    private final @Nullable PieComponent pieComponent;

    public SpoofaxTextHover(
        ISourceViewer sourceViewer,
        SpoofaxEditorBase editorBase,
        @Nullable LanguageComponent languageComponent,
        @Nullable PieComponent pieComponent
    ) {
        super(sourceViewer);
        this.editorBase = editorBase;
        this.languageComponent = languageComponent;
        this.pieComponent = pieComponent;
    }

    @Override public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
        final String baseAnnotation = super.getHoverInfo(textViewer, hoverRegion);
        final Option<HoverResult> hover = resolveHover(hoverRegion);

        if(!hover.isSome()) return baseAnnotation;

        if(baseAnnotation != null) {
            return baseAnnotation + "\n\n" + hover.unwrap().getText();
        }

        return hover.unwrap().getText();
    }

    @Override public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
        return new Region(offset, 1);
    }

    @Override protected boolean isIncluded(Annotation annotation) {
        switch(annotation.getType()) {
            case "org.eclipse.ui.workbench.texteditor.quickdiffDeletion":
            case "org.eclipse.ui.workbench.texteditor.quickdiffChange":
            case "org.eclipse.ui.workbench.texteditor.quickdiffAddition":
            case "org.eclipse.ui.workbench.texteditor.quickdiffUnchanged":
                return false;
            default:
                return true;
        }
    }

    private Option<HoverResult> resolveHover(IRegion region) {
        if(languageComponent == null || pieComponent == null || editorBase.file == null) {
            return Option.ofNone();
        }

        final ResourceKey file = new EclipseResourcePath(editorBase.file);
        final @Nullable ResourcePath rootDirectory = editorBase.project != null ? new EclipseResourcePath(editorBase.project) : null;
        final mb.common.region.Region targetRegion = mb.common.region.Region.fromOffsetLength(region.getOffset(), region.getLength());

        // We cannot do hover if we're not in a project.
        if(rootDirectory == null) {
            return Option.ofNone();
        }

        return Option.ofOptional(pieComponent.getPie().tryNewSession()).flatMap(trySession -> { // Skip hover if another session exists.
            try(final MixedSession session = trySession) {
                final TopDownSession topDownSession = session.updateAffectedBy(Collections.emptySet(), Collections.singleton(Interactivity.Interactive));
                return topDownSession.requireWithoutObserving(
                    languageComponent.getLanguageInstance().createHoverTask(rootDirectory, file, targetRegion)
                );
            } catch(ExecException e) {
                // bubble error up to eclipse, which will handle it and show a dialog
                throw new UncheckedExecException("Retrieving hover text failed unexpectedly", e);
            } catch(InterruptedException e) {
                return Option.ofNone();
            }
        });
    }
}
