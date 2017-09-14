package mb.spoofax.runtime.eclipse.editor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.FileDocumentProvider;

import mb.log.Logger;
import mb.spoofax.runtime.eclipse.util.StatusUtils;
import mb.spoofax.runtime.eclipse.vfs.EclipsePathSrv;
import mb.vfs.path.PPath;

public class DocumentProvider extends FileDocumentProvider {
    private final Logger logger;
    private final EclipsePathSrv pathSrv;


    public DocumentProvider(Logger logger, EclipsePathSrv resourceService) {
        this.logger = logger.forContext(getClass());
        this.pathSrv = resourceService;
    }


    @Override protected IDocument createDocument(Object element) throws CoreException {
        final IDocument superDocument = super.createDocument(element);
        if(superDocument != null) {
            return superDocument;
        }

        if(element instanceof IEditorInput) {
            final IDocument document = createEmptyDocument();
            final IEditorInput input = (IEditorInput) element;
            final PPath path = pathSrv.resolve(input);
            if(path == null) {
                final String message =
                    "Cannot create document for input " + element + ", could not resolve input to file object";
                logger.error(message);
                throw new CoreException(StatusUtils.error(message));
            }


            try {
                final InputStream stream = Files.newInputStream(path.getJavaPath());
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
