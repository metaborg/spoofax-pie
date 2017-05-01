package mb.pipe.run.eclipse.editor;

import java.io.InputStream;

import org.apache.commons.vfs2.FileSystemException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.FileDocumentProvider;

import mb.pipe.run.core.log.ILogger;
import mb.pipe.run.core.vfs.IResource;
import mb.pipe.run.eclipse.util.StatusUtils;
import mb.pipe.run.eclipse.vfs.IEclipseResourceSrv;

public class DocumentProvider extends FileDocumentProvider {
    private final ILogger logger;
    private final IEclipseResourceSrv resourceService;


    public DocumentProvider(ILogger logger, IEclipseResourceSrv resourceService) {
        this.logger = logger.forContext(getClass());
        this.resourceService = resourceService;
    }


    @Override protected IDocument createDocument(Object element) throws CoreException {
        final IDocument superDocument = super.createDocument(element);
        if(superDocument != null) {
            return superDocument;
        }

        if(element instanceof IEditorInput) {
            final IDocument document = createEmptyDocument();
            final IEditorInput input = (IEditorInput) element;
            final IResource resource = resourceService.resolve(input);
            if(resource == null) {
                final String message =
                    "Cannot create document for input " + element + ", could not resolve input to file object";
                logger.error(message);
                throw new CoreException(StatusUtils.error(message));
            }

            try {
                final InputStream stream = resource.fileObject().getContent().getInputStream();
                String encoding = getEncoding(element);
                if(encoding == null) {
                    encoding = getDefaultEncoding();
                }
                setDocumentContent(document, stream, encoding);
                setupDocument(element, document);
                return document;
            } catch(FileSystemException e) {
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
