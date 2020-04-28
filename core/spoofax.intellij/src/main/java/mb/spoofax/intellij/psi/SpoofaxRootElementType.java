package mb.spoofax.intellij.psi;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

public final class SpoofaxRootElementType extends SpoofaxElementType {
    public SpoofaxRootElementType(@Nullable Language language) {
        this(language, null);
    }

    public SpoofaxRootElementType(@Nullable Language language, @Nullable String debugName) {
        super(language, debugName != null ? debugName : "ROOT");
    }

    @Override public PsiElement createElement(ASTNode node) {
        return new SpoofaxPsiRootElement(node);
    }
}
