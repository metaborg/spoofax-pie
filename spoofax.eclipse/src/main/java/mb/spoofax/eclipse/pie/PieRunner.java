package mb.spoofax.eclipse.pie;

import mb.pie.api.PieSession;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.eclipse.editor.SpoofaxEditor;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;

public class PieRunner {
    public void updateEditor(
        LanguageComponent languageComponent,
        SpoofaxEditor editor,
        IEditorInput input,
        IDocument document,
        @Nullable IProject project,
        @Nullable IProgressMonitor monitor
    ) throws Exception {
        try(final PieSession session = languageComponent.newPieSession()) {

        }
    }
}
