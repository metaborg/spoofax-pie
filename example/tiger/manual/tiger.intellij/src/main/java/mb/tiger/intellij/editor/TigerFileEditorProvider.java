package mb.tiger.intellij.editor;

import mb.spoofax.intellij.editor.SpoofaxEditor;
import mb.spoofax.intellij.editor.SpoofaxFileEditorProvider;
import mb.spoofax.intellij.files.SpoofaxFileManager;


public abstract class TigerFileEditorProvider extends SpoofaxFileEditorProvider {

    public TigerFileEditorProvider(SpoofaxEditor.Factory spoofaxEditorFactory, SpoofaxFileManager spoofaxFileManager) {
        super(spoofaxEditorFactory, spoofaxFileManager);
    }

}
