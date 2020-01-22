package mb.tiger.intellij;

import com.intellij.lang.Language;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiManager;
import mb.spoofax.intellij.psi.SpoofaxFileViewProviderFactory;

import javax.annotation.Nullable;

// TODO: this class is never instantiated or bound to an extension point?
public final class TigerFileViewProviderFactory extends SpoofaxFileViewProviderFactory {
    @Override public FileViewProvider createFileViewProvider(VirtualFile file, @Nullable Language language, PsiManager manager, boolean eventSystemEnabled) {
        return new TigerFileViewProvider(manager, file, eventSystemEnabled, language);
    }
}
