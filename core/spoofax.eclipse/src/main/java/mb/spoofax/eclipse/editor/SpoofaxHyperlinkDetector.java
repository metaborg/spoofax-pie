package mb.spoofax.eclipse.editor;

import mb.common.editor.ReferenceResolutionResult;
import mb.common.option.Option;
import mb.common.region.Region;
import mb.pie.api.ExecException;
import mb.pie.api.Interactivity;
import mb.pie.api.MixedSession;
import mb.pie.api.TopDownSession;
import mb.pie.dagger.PieComponent;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.eclipse.resource.EclipseResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;

import java.util.Collections;
import java.util.Objects;

public class SpoofaxHyperlinkDetector implements IHyperlinkDetector {
    private final SpoofaxEditorBase editorBase;
    private final LanguageComponent languageComponent;
    private final PieComponent pieComponent;

    public SpoofaxHyperlinkDetector(SpoofaxEditorBase editorBase, LanguageComponent languageComponent, PieComponent pieComponent) {
        this.editorBase = editorBase;
        this.languageComponent = languageComponent;
        this.pieComponent = pieComponent;
    }

    @Override
    public @Nullable IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
        final ResourceKey file = new EclipseResourcePath(editorBase.file);
        final @Nullable ResourcePath rootDirectory = editorBase.project != null ? new EclipseResourcePath(editorBase.project) : null;
        final Region targetRegion = Region.fromOffsetLength(region.getOffset(), region.getLength());

        // We cannot do reference resolution if we're not in a project.
        if(rootDirectory == null) {
            return null;
        }

        try(final MixedSession mixedSession = pieComponent.newSession()) {
            final TopDownSession topDownSession = mixedSession.updateAffectedBy(Collections.emptySet(), Collections.singleton(Interactivity.Interactive));

            final Option<ReferenceResolutionResult> resolveResult = topDownSession.requireWithoutObserving(
                languageComponent.getLanguageInstance().createResolveTask(rootDirectory, file, targetRegion)
            );

            if(!resolveResult.isSome()) {
                return null;
            }

            return referenceResolutionResultToHyperlinks(resolveResult.get());
        } catch(ExecException | InterruptedException e) {
            return null;
        }
    }

    private @Nullable IHyperlink[] referenceResolutionResultToHyperlinks(ReferenceResolutionResult result) {
        final IHyperlink[] entries = result.getEntries().stream().map(x -> {
            if(x.getFile() instanceof EclipseResourcePath) {
                return new SpoofaxHyperlink(this.editorBase, result, x, ((EclipseResourcePath)x.getFile()).getEclipsePath());
            } else {
                return null;
            }
        }).filter(Objects::nonNull).toArray(IHyperlink[]::new);

        if(entries.length < 1) return null;

        return entries;
    }
}
