package mb.spoofax.intellij.editor;

import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider;
import com.intellij.openapi.project.Project;
import mb.common.region.Region;
import mb.common.region.Selection;
import mb.common.region.Selections;
import mb.spoofax.intellij.files.SpoofaxFileManager;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Utility functions for working with editors.
 */
public final class EditorUtils {

    /**
     * Gets the primary selection from the specified editor.
     *
     * In a single selection, this returns that selection.
     * In a multi-caret selection, this returns the primary selection.
     *
     * @param editor The editor.
     * @return The primary selection.
     */
    public static Selection getPrimarySelection(Editor editor) {
        int start = editor.getSelectionModel().getSelectionStart();
        int end = editor.getSelectionModel().getSelectionEnd();
        return Selections.region(Region.fromOffsets(start, end));
    }

    /**
     * Gets all selections from the specified editor.
     *
     * In a single selection, this returns one selection.
     * In a multi-caret selection, this returns all selections in an unspecified order.
     *
     * @param editor The editor.
     * @return The selections, in an unspecified order.
     */
    public static List<Selection> getSelections(Editor editor) {
        CaretModel caretModel = editor.getCaretModel();
        ArrayList<Selection> selections = new ArrayList<>(caretModel.getCaretCount());
        for (Caret c : caretModel.getAllCarets()) {
            int start = editor.getSelectionModel().getSelectionStart();
            int end = editor.getSelectionModel().getSelectionEnd();
            Selection selection = Selections.region(Region.fromOffsets(start, end));
            selections.add(selection);
        }

        return Collections.unmodifiableList(selections);
    }

    // https://github.com/JetBrains/kotlin/blob/37e3c41b57c92f7e03951eba22bd588840fe3088/idea/idea-jvm/src/org/jetbrains/kotlin/idea/scratch/scratchUtils.kt

    @Nullable
    public static SpoofaxIntellijFile getSpoofaxFileFromSelectedEditor(@Nullable Project project) {
        if (project == null) return null;
        @Nullable Editor selectedEditor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (selectedEditor == null) return null;
        TextEditor textEditor = TextEditorProvider.getInstance().getTextEditor(selectedEditor);
        return getSpoofaxFile(textEditor);
    }

    @Nullable
    public static SpoofaxIntellijFile getSpoofaxFile(TextEditor editor) {
        @Nullable SpoofaxEditor spoofaxEditor = findSpoofaxEditor(editor);
        if (spoofaxEditor == null) return null;
        return spoofaxEditor.getSpoofaxFile();
    }

    @Nullable
    public static SpoofaxEditor findSpoofaxEditor(TextEditor editor) {
        if (editor instanceof SpoofaxEditor) return (SpoofaxEditor)editor;
        return null;
    }
}
