package mb.spoofax.eclipse.editor;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.jface.text.DefaultTextHover;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.quickassist.IQuickAssistAssistant;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;

public class SourceViewerConfiguration extends TextSourceViewerConfiguration {
    @Override public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
        return new DefaultTextHover(sourceViewer);
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
