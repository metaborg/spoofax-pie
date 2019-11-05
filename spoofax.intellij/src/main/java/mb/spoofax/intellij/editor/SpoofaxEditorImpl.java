package mb.spoofax.intellij.editor;

import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;


public abstract class SpoofaxEditorImpl extends UserDataHolderBase implements TextEditor, SpoofaxEditor {

    private final SpoofaxIntellijFile spoofaxFile;

    public SpoofaxEditorImpl(Project project, VirtualFile file, TextEditorProvider provider, SpoofaxIntellijFile spoofaxFile) {
        super();
        this.spoofaxFile = spoofaxFile;
    }

    @Override
    public SpoofaxIntellijFile getSpoofaxFile() {
        return this.spoofaxFile;
    }

}
