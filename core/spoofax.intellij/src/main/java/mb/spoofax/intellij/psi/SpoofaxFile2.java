package mb.spoofax.intellij.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;

public final class SpoofaxFile2 extends PsiFileBase {
    private final LanguageFileType fileType;

    public SpoofaxFile2(FileViewProvider viewProvider, LanguageFileType fileType) {
        super(viewProvider, fileType.getLanguage());
        this.fileType = fileType;
    }

    @NotNull @Override public FileType getFileType() {
        return this.fileType;
    }

    @Override
    public String toString() {
        return this.getOriginalFile().getVirtualFile().getPresentableName();
    }

}
