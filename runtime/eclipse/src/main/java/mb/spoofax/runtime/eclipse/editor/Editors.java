package mb.spoofax.runtime.eclipse.editor;

import java.util.ArrayList;

public class Editors {
    private final ArrayList<SpoofaxEditor> editors = new ArrayList<>();


    public Iterable<SpoofaxEditor> editors() {
        return editors;
    }


    void addEditor(SpoofaxEditor editor) {
        editors.add(editor);
    }

    void removeEditor(SpoofaxEditor editor) {
        editors.remove(editor);
    }
}
