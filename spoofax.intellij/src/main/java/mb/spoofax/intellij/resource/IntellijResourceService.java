package mb.spoofax.intellij.resource;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiFile;
import mb.resource.DefaultResourceService;
import mb.resource.ResourceKey;
import mb.resource.ResourceRuntimeException;
import mb.spoofax.intellij.menu.ActionUtils;
import org.checkerframework.checker.nullness.qual.Nullable;


public final class IntellijResourceService {

    /**
     * Gets a resource key from a {@link VirtualFile}.
     *
     * @param virtualFile The virtual file.
     * @return The resource key.
     */
    public IntellijResourceKey getResourceKey(VirtualFile virtualFile) {
        // The URL of a VirtualFile is in the form "protocol://path",
        // where 'protocol' uniquely identifies a VirtualFileSystem
        // and 'path' is a path within that file system.
        String url = virtualFile.getUrl();
        return new IntellijResourceKey(url);
    }

    /**
     * Gets a resource key from a {@link Document}.
     *
     * @param document The document.
     * @return The resource key; or {@code null} when it could not be determined.
     */
    @Nullable
    public IntellijResourceKey getResourceKey(Document document) {
        @Nullable VirtualFile file = FileDocumentManager.getInstance().getFile(document);
        if(file == null) return null;
        return getResourceKey(file);
    }

    /**
     * Gets a resource key from a {@link PsiFile}.
     *
     * @param psiFile The PSI file.
     * @return The resource key; or {@code null} when it could not be determined.
     */
    @Nullable
    public IntellijResourceKey getResourceKey(PsiFile psiFile) {
        @Nullable VirtualFile file = psiFile.getOriginalFile().getVirtualFile();
        if(file == null) return null;
        return getResourceKey(file);
    }

    /**
     * Gets a resource key from an {@link AnActionEvent}.
     *
     * @param e The action event.
     * @return The resource key; or {@code null} when it could not be determined.
     */
    @Nullable
    public IntellijResourceKey getResourceKey(AnActionEvent e) {
        @Nullable PsiFile psiFile = ActionUtils.getPsiFile(e);
        if (psiFile == null) return null;
        return getResourceKey(psiFile);
    }

}
