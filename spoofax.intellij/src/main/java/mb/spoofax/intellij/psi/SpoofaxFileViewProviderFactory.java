package mb.spoofax.intellij.psi;

import com.intellij.lang.Language;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.FileViewProviderFactory;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;


public abstract class SpoofaxFileViewProviderFactory implements FileViewProviderFactory {

    @Override
    public abstract FileViewProvider createFileViewProvider(VirtualFile file, @Nullable Language language, PsiManager manager, boolean eventSystemEnabled);

}
