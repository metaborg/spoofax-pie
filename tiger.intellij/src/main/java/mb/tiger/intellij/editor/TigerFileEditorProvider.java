package mb.tiger.intellij.editor;

import mb.spoofax.intellij.editor.SpoofaxEditor;
import mb.spoofax.intellij.editor.SpoofaxFileEditorProvider;
import mb.spoofax.intellij.files.ISpoofaxFileManager;


public abstract class TigerFileEditorProvider extends SpoofaxFileEditorProvider {

    public TigerFileEditorProvider(SpoofaxEditor.Factory spoofaxEditorFactory, ISpoofaxFileManager spoofaxFileManager) {
        super(spoofaxEditorFactory, spoofaxFileManager);
    }

}
