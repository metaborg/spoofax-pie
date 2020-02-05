package mb.spoofax.eclipse.editor;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.texteditor.AbstractDocumentProvider;

public class NamedDocumentProvider extends AbstractDocumentProvider {
    @Override protected IDocument createDocument(Object element) {
        return new Document();
    }

    @Override protected @Nullable IAnnotationModel createAnnotationModel(Object element) {
        return null;
    }

    @Override
    protected void doSaveDocument(IProgressMonitor monitor, Object element, IDocument document, boolean overwrite) {

    }

    @Override protected @Nullable IRunnableContext getOperationRunner(IProgressMonitor monitor) {
        return null;
    }


    @Override public boolean mustSaveDocument(Object element) {
        return false;
    }

    @Override public boolean canSaveDocument(Object element) {
        return false;
    }
}
