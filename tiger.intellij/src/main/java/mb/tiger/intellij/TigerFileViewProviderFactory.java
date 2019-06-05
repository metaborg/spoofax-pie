package mb.tiger.intellij;

import com.intellij.lang.Language;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiManager;
import mb.spoofax.intellij.psi.SpoofaxFileViewProviderFactory;

import javax.annotation.Nullable;

public final class TigerFileViewProviderFactory extends SpoofaxFileViewProviderFactory {
    @Override public FileViewProvider createFileViewProvider(VirtualFile file,
        @Nullable Language language, PsiManager manager, boolean eventSystemEnabled) {
        assert language != null;
        return new TigerFileViewProvider(manager, file, eventSystemEnabled, language);
    }
}
