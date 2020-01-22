package mb.spoofax.intellij.psi;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.PsiFileImpl;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.psi.tree.IElementType;
import mb.spoofax.intellij.IntellijLanguageComponent;

public abstract class SpoofaxFile extends PsiFileImpl implements PsiFile {
    private final IntellijLanguageComponent languageComponent;

    public SpoofaxFile(FileViewProvider provider, IntellijLanguageComponent languageComponent) {
        super(languageComponent.getFileElementType(), languageComponent.getFileElementType(), provider);
        this.languageComponent = languageComponent;
    }

    @Override public FileType getFileType() {
        return languageComponent.getFileType();
    }

    @Override public void accept(PsiElementVisitor visitor) {
        visitor.visitFile(this);
    }

    @Override public PsiReference[] getReferences() {
        return ReferenceProvidersRegistry.getReferencesFromProviders(this);
    }
}
