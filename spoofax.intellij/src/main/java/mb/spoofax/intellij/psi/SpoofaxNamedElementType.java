package mb.spoofax.intellij.psi;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;


public class SpoofaxNamedElementType extends SpoofaxElementType {
    public SpoofaxNamedElementType(@Nullable Language language) {
        this(language, null);
    }

    public SpoofaxNamedElementType(@Nullable Language language, @Nullable String debugName) {
        super(language, debugName != null ? debugName : "NAMED");
    }

    @Override public PsiElement createElement(ASTNode node) {
        return new SpoofaxPsiNamedElement(node);
    }
}
