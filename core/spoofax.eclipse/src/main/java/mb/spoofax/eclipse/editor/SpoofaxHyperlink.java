package mb.spoofax.eclipse.editor;

import mb.common.editor.ReferenceResolutionResult;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.spoofax.eclipse.util.UncheckedCoreException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

public class SpoofaxHyperlink implements IHyperlink {
    private final SpoofaxEditorBase textEditor;
    private final ReferenceResolutionResult result;
    private final ReferenceResolutionResult.ResolvedEntry entry;
    private final IPath path;

    public SpoofaxHyperlink(SpoofaxEditorBase textEditor, ReferenceResolutionResult result, ReferenceResolutionResult.ResolvedEntry entry, IPath path) {
        this.textEditor = textEditor;
        this.result = result;
        this.entry = entry;
        this.path = path;
    }

    @Override public IRegion getHyperlinkRegion() {
        return new Region(this.result.getHighlightedRegion().getStartOffset(), this.result.getHighlightedRegion().getLength());
    }

    @Override public String getTypeLabel() {
        return null;
    }

    @Override public String getHyperlinkText() {
        return this.entry.getLabel();
    }

    @Override public void open() {
        final @Nullable IFile editorFile = textEditor.file;

        if(editorFile != null && editorFile.getFullPath().equals(this.path)) {
            // If the reference goes to the same file, re-navigate this
            // editor instead of opening a new one.
            textEditor.selectAndReveal(
                this.entry.getRegion().getStartOffset(),
                this.entry.getRegion().getLength()
            );
            textEditor.setFocus();
        } else {
            // Open a new editor containing the specified file.
            this.openInNewEditor();
        }
    }

    private void openInNewEditor() {
        final Display display = Display.getDefault();
        display.asyncExec(() -> {
            final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            try {
                final IEditorPart editorPart = IDE.openEditor(
                    page,
                    SpoofaxPlugin.getPlatformComponent().getResourceUtil().getEclipseFile(entry.getFile())
                );

                if(editorPart instanceof ITextEditor) {
                    final ITextEditor editor = (ITextEditor)editorPart;

                    editor.selectAndReveal(
                        this.entry.getRegion().getStartOffset(),
                        this.entry.getRegion().getLength()
                    );
                    editor.setFocus();
                }
            } catch(CoreException e) {
                // Nothing we can do here.
                throw new UncheckedCoreException(e);
            }
        });
    }
}
