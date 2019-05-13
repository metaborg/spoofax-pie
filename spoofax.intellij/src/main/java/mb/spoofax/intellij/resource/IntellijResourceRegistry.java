package mb.spoofax.intellij.resource;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiFile;
import groovy.lang.Singleton;
import mb.resource.Resource;
import mb.resource.ResourceRegistry;
import mb.resource.ResourceRuntimeException;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

public final class IntellijResourceRegistry implements ResourceRegistry {
    protected static final String qualifier = "eclipse-resource";


    public IntellijResource getResource(VirtualFile virtualFile) {
        return new IntellijResource(virtualFile);
    }

    public IntellijResource getResource(String url) {
        final @Nullable VirtualFile file = VirtualFileManager.getInstance().findFileByUrl(url);
        if(file == null) {
            throw new ResourceRuntimeException(
                "Cannot get IntelliJ resource for URL '" + url + "'; no file was found");
        }
        return getResource(file);
    }

    public IntellijResource getResource(Document document) {
        final @Nullable VirtualFile file = FileDocumentManager.getInstance().getFile(document);
        if(file == null) {
            throw new ResourceRuntimeException(
                "Cannot get IntelliJ resource for document '" + document + "'; it is not associated to a file");
        }
        return getResource(file);
    }

    public IntellijResource getResource(PsiFile psiFile) {
        final @Nullable VirtualFile file = psiFile.getVirtualFile();
        if(file == null) {
            throw new ResourceRuntimeException(
                "Cannot get IntelliJ resource for Psi file '" + psiFile + "'; the file exists only in memory");
        }
        return getResource(file);
    }


    @Override public Serializable qualifier() {
        return qualifier;
    }

    @Override public Resource getResource(Serializable id) {
        if(!(id instanceof String)) {
            throw new ResourceRuntimeException(
                "Cannot get IntelliJ resource with ID '" + id + "'; the ID is not of type String");
        }
        final String url = (String) id;
        return getResource(url);
    }
}
