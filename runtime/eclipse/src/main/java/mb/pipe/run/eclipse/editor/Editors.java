package mb.pipe.run.eclipse.editor;

import java.util.ArrayList;

public class Editors {
    private final ArrayList<PipeEditor> editors = new ArrayList<>();


    public Iterable<PipeEditor> editors() {
        return editors;
    }


    void addEditor(PipeEditor editor) {
        editors.add(editor);
    }

    void removeEditor(PipeEditor editor) {
        editors.remove(editor);
    }
}
