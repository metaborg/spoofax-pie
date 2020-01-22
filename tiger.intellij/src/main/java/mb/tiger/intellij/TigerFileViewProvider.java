package mb.tiger.intellij;

import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.SingleRootFileViewProvider;
import mb.spoofax.intellij.psi.SpoofaxFileViewProvider;

// TODO: this class is never instantiated or bound to an extension point?
public class TigerFileViewProvider extends SpoofaxFileViewProvider {
    public TigerFileViewProvider(PsiManager manager, VirtualFile virtualFile, boolean eventSystemEnabled, Language language) {
        super(manager, virtualFile, eventSystemEnabled, language);
    }

    @Override protected PsiFile createFile(Project project, VirtualFile file, FileType fileType) {
        return new TigerFile(this);
    }

    @Override public SingleRootFileViewProvider createCopy(VirtualFile copy) {
        return new TigerFileViewProvider(this.getManager(), copy, false, this.getBaseLanguage());
    }
}
