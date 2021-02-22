package mb.spoofax.intellij.psi;

import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilderFactory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IFileElementType;
import mb.resource.Resource;
import mb.spoofax.intellij.IntellijLanguage;
import mb.spoofax.intellij.editor.SpoofaxLexer;
import mb.spoofax.intellij.editor.SpoofaxLexerFactory;
import mb.spoofax.intellij.resource.IntellijResourceRegistry;

import javax.inject.Inject;

public class SpoofaxFileElementType extends IFileElementType {
    private final SpoofaxLexerFactory lexerFactory;
    private final IntellijResourceRegistry resourceRegistry;
    private final SpoofaxAstBuilder.Factory astBuilderFactory;


    @Inject public SpoofaxFileElementType(
        IntellijLanguage language,
        SpoofaxLexerFactory lexerFactory,
        IntellijResourceRegistry resourceRegistry,
        SpoofaxAstBuilder.Factory astBuilderFactory
    ) {
        super(language);
        this.lexerFactory = lexerFactory;
        this.resourceRegistry = resourceRegistry;
        this.astBuilderFactory = astBuilderFactory;
    }

    @Override protected ASTNode doParseContents(ASTNode chameleon, PsiElement psi) {
        PsiFile containingFile = psi.getContainingFile();
        Resource resource = this.resourceRegistry.getResource(containingFile);
        SpoofaxLexer lexer = this.lexerFactory.create(resource.getKey());
        PsiBuilder builder =
            PsiBuilderFactory.getInstance().createBuilder(psi.getProject(), chameleon, lexer, getLanguage(),
                chameleon.getChars());
        SpoofaxAstBuilder astBuilder = this.astBuilderFactory.create();
        ASTNode tree = astBuilder.build(this, builder);
        return tree.getFirstChildNode();
    }
}
