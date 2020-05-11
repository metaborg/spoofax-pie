package mb.spoofax.intellij.editor;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import mb.spoofax.intellij.IntellijLanguageComponent;
import mb.spoofax.intellij.psi.SpoofaxElementType;
import mb.spoofax.intellij.psi.SpoofaxFile2;
import mb.spoofax.intellij.psi.SpoofaxTokenTypeManager;

import javax.inject.Inject;

public class SpoofaxParserDefinition implements ParserDefinition {
    private final LanguageFileType fileType;
    private final IFileElementType fileElementType;
    private final SpoofaxTokenTypeManager tokenTypeManager;


    public SpoofaxParserDefinition(IntellijLanguageComponent languageComponent) {
        this.fileType = languageComponent.getFileType();
        this.fileElementType = languageComponent.getFileElementType();
        this.tokenTypeManager = new SpoofaxTokenTypeManager(languageComponent.getLanguage());
    }


    @Override public IFileElementType getFileNodeType() {
        return this.fileElementType;
    }

    @Override public SpaceRequirements spaceExistenceTypeBetweenTokens(ASTNode left, ASTNode right) {
        return SpaceRequirements.MAY;
    }

    @Override public TokenSet getStringLiteralElements() {
        return this.tokenTypeManager.getStringLiteralTokens();
    }

    @Override public TokenSet getWhitespaceTokens() {
        return this.tokenTypeManager.getWhitespaceTokens();
    }

    @Override public TokenSet getCommentTokens() {
        return this.tokenTypeManager.getCommentTokens();
    }

    @Override public Lexer createLexer(Project project) {
        throw new UnsupportedOperationException("See SpoofaxFileElementType.doParseContents().");
    }

    @Override public PsiParser createParser(Project project) {
        throw new UnsupportedOperationException("See SpoofaxFileElementType.doParseContents().");
    }

    @Override public PsiFile createFile(FileViewProvider viewProvider) {
        return new SpoofaxFile2(viewProvider, this.fileType);
    }

    @Override public PsiElement createElement(ASTNode node) {
        IElementType elementType = node.getElementType();
        if(!(elementType instanceof SpoofaxElementType))
            throw new UnsupportedOperationException("Unexpected element type: " + elementType);
        SpoofaxElementType spoofaxElementType = (SpoofaxElementType) elementType;
        return spoofaxElementType.createElement(node);
    }
}
