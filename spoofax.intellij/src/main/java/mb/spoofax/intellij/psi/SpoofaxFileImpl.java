package mb.spoofax.intellij.psi;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.PsiFileImpl;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.psi.tree.IElementType;

public abstract class SpoofaxFileImpl extends PsiFileImpl implements SpoofaxFile {
    public SpoofaxFileImpl(IElementType elementType, FileViewProvider provider) {
        super(elementType, elementType, provider);
    }

    @Override public abstract FileType getFileType();

    @Override public void accept(PsiElementVisitor visitor) {
        visitor.visitFile(this);
    }

    @Override public PsiReference[] getReferences() {
        return ReferenceProvidersRegistry.getReferencesFromProviders(this);
    }
}
