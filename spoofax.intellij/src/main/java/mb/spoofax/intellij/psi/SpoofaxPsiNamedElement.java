package mb.spoofax.intellij.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;


public final class SpoofaxPsiNamedElement extends SpoofaxPsiElement implements PsiNamedElement {


    public SpoofaxPsiNamedElement(ASTNode node) {
        super(node);
    }

    @Override
    public String getName() {
        return this.getNode().getText();
    }

    @Override
    public PsiElement setName(String name) throws IncorrectOperationException {
        throw new IncorrectOperationException();
    }

}
