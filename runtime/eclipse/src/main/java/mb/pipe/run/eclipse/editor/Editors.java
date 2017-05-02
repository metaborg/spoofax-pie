package mb.pipe.run.eclipse.editor;

import java.util.Collection;

import com.google.common.collect.Lists;

public class Editors {
    private final Collection<PipeEditor> editors = Lists.newArrayList();


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
