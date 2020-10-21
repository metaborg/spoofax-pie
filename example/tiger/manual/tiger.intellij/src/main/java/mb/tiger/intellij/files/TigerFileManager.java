package mb.tiger.intellij.files;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import mb.spoofax.intellij.editor.SpoofaxIntellijFile;
import mb.spoofax.intellij.files.SpoofaxFileManager;
import mb.spoofax.intellij.files.SpoofaxFileLanguageProvider;
import org.checkerframework.checker.nullness.qual.Nullable;


public final class TigerFileManager implements SpoofaxFileManager {

    @Override
    public @Nullable SpoofaxIntellijFile tryCreate(Project project, VirtualFile file) {
        @Nullable PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        if (psiFile == null) return null;
        @Nullable SpoofaxFileLanguageProvider languageProvider = SpoofaxFileLanguageProvider.get(psiFile.getLanguage());
        if (languageProvider == null) return null;
        return languageProvider.newSpoofaxFile(project, file);
    }

}
