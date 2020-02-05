package mb.spoofax.intellij.psi;

import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;
import mb.spoofax.core.language.LanguageScope;

import javax.inject.Inject;

public final class SpoofaxAstBuilder {
    @LanguageScope
    public static class Factory {
        private final SpoofaxElementTypeManager elementTypeManager;

        @Inject public Factory(SpoofaxElementTypeManager elementTypeManager) {
            this.elementTypeManager = elementTypeManager;
        }

        public SpoofaxAstBuilder create() {
            return new SpoofaxAstBuilder(this.elementTypeManager);
        }
    }


    private final SpoofaxElementTypeManager elementTypeManager;


    @Inject public SpoofaxAstBuilder(SpoofaxElementTypeManager elementTypeManager) {
        this.elementTypeManager = elementTypeManager;
    }


    public ASTNode build(IElementType root, PsiBuilder builder) {
        PsiBuilder.Marker m = builder.mark();
        // Add sub-root to prevent ASTNode.firstChild from being null.
        PsiBuilder.Marker m2 = builder.mark();
        while(!builder.eof()) {
            PsiBuilder.Marker m3 = builder.mark();
            IElementType elementType =
                this.elementTypeManager.getElementType((SpoofaxTokenType) builder.getTokenType());
            builder.advanceLexer();
            m3.done(elementType);
        }
        m2.done(elementTypeManager.getRootElementType());
        m.done(root);

        return builder.getTreeBuilt();
    }
}
