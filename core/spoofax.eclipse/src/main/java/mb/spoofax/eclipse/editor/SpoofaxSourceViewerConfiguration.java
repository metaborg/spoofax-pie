package mb.spoofax.eclipse.editor;

import mb.pie.dagger.PieComponent;
import mb.spoofax.core.language.LanguageComponent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.jface.internal.text.html.HTMLTextPresenter;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.quickassist.IQuickAssistAssistant;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;

import java.util.Arrays;

public class SpoofaxSourceViewerConfiguration extends TextSourceViewerConfiguration {
    private final SpoofaxEditorBase editorBase;
    private final @Nullable LanguageComponent languageComponent;
    private final @Nullable PieComponent pieComponent;

    public SpoofaxSourceViewerConfiguration(SpoofaxEditorBase editorBase, @Nullable LanguageComponent languageComponent, @Nullable PieComponent pieComponent) {
        this.editorBase = editorBase;
        this.languageComponent = languageComponent;
        this.pieComponent = pieComponent;
    }

    @Override public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
        return new SpoofaxTextHover(sourceViewer, editorBase, languageComponent, pieComponent);
    }

    public IInformationControlCreator getInformationControlCreator(ISourceViewer sourceViewer) {
        return parent -> new DefaultInformationControl(parent, new HTMLTextPresenter(true));
    }

    @Override public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
        final IHyperlinkDetector[] detectors = super.getHyperlinkDetectors(sourceViewer);
        if(languageComponent == null || pieComponent == null) return detectors;

        final IHyperlinkDetector[] newDetectors = Arrays.copyOf(detectors, detectors.length + 1);
        newDetectors[detectors.length] = new SpoofaxHyperlinkDetector(editorBase, languageComponent, pieComponent);

        return newDetectors;
    }

    @Override public @Nullable IReconciler getReconciler(@NonNull ISourceViewer sourceViewer) {
        // Return null to disable TextSourceViewerConfiguration reconciler which does spell checking.
        return null;
    }

    @Override public @Nullable IQuickAssistAssistant getQuickAssistAssistant(ISourceViewer sourceViewer) {
        // Return null to disable TextSourceViewerConfiguration quick assist which does spell checking.
        return null;
    }
}
