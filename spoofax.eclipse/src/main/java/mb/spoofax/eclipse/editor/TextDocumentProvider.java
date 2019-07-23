package mb.spoofax.eclipse.editor;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.editors.text.StorageDocumentProvider;

import java.util.concurrent.ConcurrentHashMap;

public class TextDocumentProvider extends StorageDocumentProvider {
    private static final ConcurrentHashMap<String, String> nameToText = new ConcurrentHashMap<>();

    public static TextEditorInput createTextEditorInput(String name, String text) {
        nameToText.put(name, text);
        return new TextEditorInput(name);
    }

    static String getText(String name) {
        final @Nullable String text = nameToText.get(name);
        if(text == null) {
            throw new RuntimeException("No text was found for TextEditorInput with name '" + name + "'");
        }
        return text;
    }


    @Override protected void disposeElementInfo(Object element, ElementInfo info) {
        super.disposeElementInfo(element, info);
        if(element instanceof TextEditorInput) {
            final TextEditorInput textEditorInput = (TextEditorInput) element;
            nameToText.remove(textEditorInput.getName());
        }
    }

    @Override public IDocument getDocument(Object element) {
        final IDocument document = super.getDocument(element);
        if(element instanceof TextEditorInput) {
            final TextEditorInput textEditorInput = (TextEditorInput) element;
            final String newText = nameToText.get(textEditorInput.getName());
            final String documentText = document.get();
            if(!newText.equals(documentText)) {
                document.set(newText);
                try {
                    saveDocument(null, element, document, true);
                } catch(CoreException e) {
                    handleCoreException(e, "Saving text document failed");
                }
            }
        }
        return document;
    }
}
