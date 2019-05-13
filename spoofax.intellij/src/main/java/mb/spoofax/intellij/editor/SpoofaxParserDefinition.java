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
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import mb.spoofax.intellij.psi.SpoofaxTokenTypeManager;

import javax.inject.Inject;

public class SpoofaxParserDefinition implements ParserDefinition {
    private final LanguageFileType fileType;
    private final IFileElementType fileElementType;
    private final SpoofaxTokenTypeManager tokenTypeManager;

    @Inject
    public SpoofaxParserDefinition(
        LanguageFileType fileType,
        IFileElementType fileElementType,
        SpoofaxTokenTypeManager tokenTypeManager
    ) {
        this.fileType = fileType;
        this.fileElementType = fileElementType;
        this.tokenTypeManager = tokenTypeManager;
    }

    @Override
    public IFileElementType getFileNodeType() {
        return this.fileElementType;
    }

    @Override
    public SpaceRequirements spaceExistenceTypeBetweenTokens(ASTNode left, ASTNode right) {
        return SpaceRequirements.MAY;
    }

    @Override
    public TokenSet getStringLiteralElements() {
        return this.tokenTypeManager.getStringLiteralTokens();
    }

    @Override
    public TokenSet getWhitespaceTokens() {
        return this.tokenTypeManager.getWhitespaceTokens();
    }

    @Override
    public TokenSet getCommentTokens() {
        return this.tokenTypeManager.getCommentTokens();
    }

    @Override
    public Lexer createLexer(Project project) {
        throw new UnsupportedOperationException("See SpoofaxFileElementType.doParseContents().");
    }

    @Override
    public PsiParser createParser(Project project) {
        throw new UnsupportedOperationException("See SpoofaxFileElementType.doParseContents().");
    }

    @Override
    public PsiFile createFile(FileViewProvider viewProvider) {
        // TODO: fix code
//        return new SpoofaxFile2(viewProvider, this.fileType);
        throw new UnsupportedOperationException();
    }

    @Override
    public PsiElement createElement(ASTNode node) {
        // TODO: fix code
//        IElementType elementType = node.getElementType();
//        if(!(elementType instanceof SpoofaxElementType))
//            throw new UnsupportedOperationException("Unexpected element type: " + elementType);
//        SpoofaxElementType spoofaxElementType = (SpoofaxElementType) elementType;
//        return spoofaxElementType.createElement(node);
        throw new UnsupportedOperationException();
    }
}
