package mb.spoofax.intellij.resource;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiFile;
import mb.resource.ResourceKey;
import mb.resource.ResourceKeyString;
import mb.resource.ResourceRegistry;
import mb.resource.ResourceRuntimeException;
import mb.spoofax.intellij.menu.ActionUtils;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * IntelliJ resource registry.
 */
public final class IntellijResourceRegistry implements ResourceRegistry {

    /* package private */ static final String qualifier = "intellij-resource";

    /**
     * Gets a resource from an URL.
     *
     * @param url the URL
     * @return the resource
     */
    public IntellijResource getResource(String url) {
        final @Nullable VirtualFile file = VirtualFileManager.getInstance().findFileByUrl(url);
        if(file == null) {
            throw new ResourceRuntimeException("Cannot get IntelliJ resource for URL '" + url + "'; no file was found");
        }
        return getResource(file);
    }

    /**
     * Gets a resource from a {@link VirtualFile}.
     *
     * @param virtualFile the virtual file
     * @return the resource
     */
    public IntellijResource getResource(VirtualFile virtualFile) {
        return new IntellijResource(virtualFile);
    }

    /**
     * Gets a resource from a {@link Document}.
     *
     * @param document the document
     * @return the resource
     */
    public IntellijResource getResource(Document document) {
        final @Nullable VirtualFile file = FileDocumentManager.getInstance().getFile(document);
        if(file == null) {
            throw new ResourceRuntimeException("Cannot get IntelliJ resource for document '" + document + "'; it is not associated with a file.");
        }
        return getResource(file);
    }

    /**
     * Gets a resource from a {@link PsiFile}.
     *
     * @param psiFile the PSI file
     * @return the resource
     */
    public IntellijResource getResource(PsiFile psiFile) {
        final @Nullable VirtualFile file = psiFile.getOriginalFile().getVirtualFile();
        if(file == null) {
            throw new ResourceRuntimeException("Cannot get IntelliJ resource for Psi file '" + psiFile.getName() + "'; the file exists only in memory.");
        }
        return getResource(file);
    }

    /**
     * Gets a resource from an {@link AnActionEvent}.
     *
     * @param e the action event
     * @return the resource
     */
    public IntellijResource getResource(AnActionEvent e) {
        @Nullable PsiFile psiFile = ActionUtils.getPsiFile(e);
        if (psiFile == null) {
            throw new ResourceRuntimeException("Cannot get IntelliJ resource for event '" + e + "'; it is not associated with a file.");
        }
        return getResource(psiFile);
    }

    @Override public String qualifier() {
        return qualifier;
    }

    @Override public IntellijResourceKey getResourceKey(ResourceKeyString keyStr) {
        if(!keyStr.qualifierMatchesOrMissing(qualifier)) {
            throw new ResourceRuntimeException("Qualifier of '" + keyStr + "' does not match qualifier '" + qualifier + "' of this resource registry.");
        }
        final String url = keyStr.getId();
        return new IntellijResourceKey(url);
    }

    @Override public IntellijResource getResource(ResourceKey key) {
        if(!(key instanceof IntellijResourceKey)) {
            throw new ResourceRuntimeException("Cannot get IntelliJ resource for key '" + key + "'; it is not of type IntellijResourceKey.");
        }
        final IntellijResourceKey intellijResourceKey = (IntellijResourceKey)key;
        return getResource(intellijResourceKey.getId());
    }

    @Override public IntellijResource getResource(ResourceKeyString keyStr) {
        if(!keyStr.qualifierMatchesOrMissing(qualifier)) {
            throw new ResourceRuntimeException("Qualifier of '" + keyStr + "' does not match qualifier '" + qualifier + "' of this resource registry.");
        }
        final String url = keyStr.getId();
        return getResource(url);
    }
}
