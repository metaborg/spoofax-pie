package mb.spoofax.intellij.psi;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.Nullable;

public class SpoofaxElementType extends IElementType {
    public SpoofaxElementType(@Nullable Language language) {
        this(language, null);
    }

    public SpoofaxElementType(@Nullable Language language, @Nullable String debugName) {
        super(debugName != null ? debugName : "CONTENT", language);
    }

    public PsiElement createElement(ASTNode node) {
        return new SpoofaxPsiElement(node);
    }
}
