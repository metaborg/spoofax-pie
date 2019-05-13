package mb.spoofax.intellij.psi;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilderFactory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IFileElementType;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.intellij.editor.SpoofaxLexer;
import mb.spoofax.intellij.resource.IntellijResource;
import mb.spoofax.intellij.resource.IntellijResourceRegistry;

import javax.inject.Inject;

@LanguageScope
public class SpoofaxFileElementType extends IFileElementType {

    private final SpoofaxLexer.Factory lexerFactory;
    private final IntellijResourceRegistry resourceRegistry;
    private final SpoofaxAstBuilder.Factory astBuilderFactory;
    private final LanguageComponent languageComponent;

    @Inject
    public SpoofaxFileElementType(Language language, SpoofaxLexer.Factory lexerFactory, IntellijResourceRegistry resourceRegistry, SpoofaxAstBuilder.Factory astBuilderFactory, LanguageComponent languageComponent) {
        super(language);

        this.lexerFactory = lexerFactory;
        this.resourceRegistry = resourceRegistry;
        this.astBuilderFactory = astBuilderFactory;
        this.languageComponent = languageComponent;
    }

    @Override
    protected ASTNode doParseContents(ASTNode chameleon, PsiElement psi) {
        IntellijResource resource = this.resourceRegistry.getResource(psi.getContainingFile());
        SpoofaxLexer lexer = this.lexerFactory.create(this.languageComponent, resource.getKey());
        PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(psi.getProject(), chameleon, lexer, getLanguage(), chameleon.getChars());
        SpoofaxAstBuilder astBuilder = this.astBuilderFactory.create();
        ASTNode tree = astBuilder.build(this, builder);
        return tree.getFirstChildNode();
    }

}
