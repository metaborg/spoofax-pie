package mb.spoofax.runtime.eclipse.editor;

import java.io.IOException;
import java.io.InputStream;
import mb.fs.java.JavaFSNode;
import mb.log.api.Logger;
import mb.spoofax.runtime.eclipse.util.FileUtils;
import mb.spoofax.runtime.eclipse.util.StatusUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.FileDocumentProvider;

public class DocumentProvider extends FileDocumentProvider {
    private final Logger logger;
    private final FileUtils fileUtils;


    public DocumentProvider(Logger logger, FileUtils fileUtils) {
        this.logger = logger.forContext(getClass());
        this.fileUtils = fileUtils;
    }


    @Override protected IDocument createDocument(Object element) throws CoreException {
        final IDocument superDocument = super.createDocument(element);
        if(superDocument != null) {
            return superDocument;
        }

        if(element instanceof IEditorInput) {
            final IDocument document = createEmptyDocument();
            final IEditorInput input = (IEditorInput) element;
            final JavaFSNode node = fileUtils.toNode(input);
            if(node == null) {
                final String message =
                    "Cannot create document for input " + element + ", could not resolve input to file object";
                logger.error(message);
                throw new CoreException(StatusUtils.error(message));
            }


            try {
                final InputStream stream = node.newInputStream();
                String encoding = getEncoding(element);
                if(encoding == null) {
                    encoding = getDefaultEncoding();
                }
                setDocumentContent(document, stream, encoding); // Stream is closed here
                setupDocument(element, document);
                return document;
            } catch(IOException e) {
                final String message = "Cannot create document for input " + element;
                logger.error(message, e);
                throw new CoreException(StatusUtils.error(message, e));
            }
        }

        return null;
    }

    @Override protected IDocument createEmptyDocument() {
        return super.createEmptyDocument();
    }
}
