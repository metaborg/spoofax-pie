package mb.spoofax.eclipse.editor;

import org.eclipse.jface.text.DefaultTextHover;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.ISourceViewer;

public class SpoofaxTextHover extends DefaultTextHover {
    public SpoofaxTextHover(ISourceViewer sourceViewer) {
        super(sourceViewer);
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
}
