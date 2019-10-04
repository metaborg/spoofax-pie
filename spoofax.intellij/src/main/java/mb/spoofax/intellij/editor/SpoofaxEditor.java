package mb.spoofax.intellij.editor;

import com.intellij.openapi.fileEditor.TextEditor;


/**
 * A Spoofax file editor.
 */
public interface SpoofaxEditor extends TextEditor {

    interface Factory {
        SpoofaxEditor create(SpoofaxIntellijFile spoofaxFile);
    }

    SpoofaxIntellijFile getSpoofaxFile();
}
