package mb.tiger.intellij.editor;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.TextEditorWithPreview;
import com.intellij.pom.Navigatable;
import org.jetbrains.annotations.NotNull;


public class TigerFileEditorWithPreview extends TextEditorWithPreview implements TextEditor {

    private final TextEditor sourceTextEditor;
    private final TextEditor previewTextEditor;

    public TigerFileEditorWithPreview(TextEditor sourceTextEditor, TextEditor previewTextEditor) {
        super(sourceTextEditor, previewTextEditor);
        this.sourceTextEditor = sourceTextEditor;
        this.previewTextEditor = previewTextEditor;

    }

    @NotNull
    @Override
    public Editor getEditor() {
        return null;
    }

    @Override
    public boolean canNavigateTo(@NotNull Navigatable navigatable) {
        return false;
    }

    @Override
    public void navigateTo(@NotNull Navigatable navigatable) {

    }

}
