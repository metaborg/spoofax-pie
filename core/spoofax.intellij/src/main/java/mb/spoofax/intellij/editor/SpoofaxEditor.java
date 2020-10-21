package mb.spoofax.intellij.editor;

import com.intellij.openapi.fileEditor.TextEditor;
import mb.spoofax.intellij.files.SpoofaxFileManager;


/**
 * A Spoofax file editor.
 */
public interface SpoofaxEditor extends TextEditor {

    interface Factory {
        SpoofaxEditor create(SpoofaxIntellijFile spoofaxFile);
    }

    SpoofaxIntellijFile getSpoofaxFile();
}
