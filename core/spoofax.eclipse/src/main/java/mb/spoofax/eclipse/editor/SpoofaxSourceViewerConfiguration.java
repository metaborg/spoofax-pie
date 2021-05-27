package mb.spoofax.eclipse.editor;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.jface.internal.text.html.HTMLTextPresenter;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.quickassist.IQuickAssistAssistant;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;

public class SpoofaxSourceViewerConfiguration extends TextSourceViewerConfiguration {
    @Override public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
        return new SpoofaxTextHover(sourceViewer);
    }

    public IInformationControlCreator getInformationControlCreator(ISourceViewer sourceViewer) {
        return parent -> new DefaultInformationControl(parent, new HTMLTextPresenter(true));
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
