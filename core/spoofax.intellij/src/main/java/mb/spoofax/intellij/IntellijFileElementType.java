package mb.spoofax.intellij;

import com.intellij.lang.ASTFactory;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;

public class IntellijFileElementType extends IFileElementType {
    private final IntellijLanguageComponent languageComponent;

    public IntellijFileElementType(IntellijLanguageComponent languageComponent) {
        super(languageComponent.getLanguageInstance().getDisplayName() + "_FILE", languageComponent.getLanguage());
        this.languageComponent = languageComponent;
    }

    @Override public ASTNode parseContents(ASTNode chameleon) {
        return ASTFactory.leaf(new IElementType(languageComponent.getLanguageInstance().getDisplayName() + "_TEXT", languageComponent.getLanguage()), chameleon.getChars());
    }
}
