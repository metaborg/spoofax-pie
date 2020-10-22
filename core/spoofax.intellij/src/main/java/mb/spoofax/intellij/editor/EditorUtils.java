package mb.spoofax.intellij.editor;

import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import mb.common.region.Region;
import mb.common.region.Selection;
import mb.common.region.Selections;

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

}
