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
import org.jetbrains.annotations.NotNull;

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

    @NotNull
    @Override
    public TokenSet getStringLiteralElements() {
        return this.tokenTypeManager.getStringLiteralTokens();
    }

    @NotNull
    @Override
    public TokenSet getWhitespaceTokens() {
        return this.tokenTypeManager.getWhitespaceTokens();
    }

    @NotNull
    @Override
    public TokenSet getCommentTokens() {
        return this.tokenTypeManager.getCommentTokens();
    }

    @NotNull
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
        return new SpoofaxFile2(viewProvider, this.fileType);
    }

    @NotNull
    @Override
    public PsiElement createElement(ASTNode node) {
        IElementType elementType = node.getElementType();
        if (!(elementType instanceof SpoofaxElementType))
            throw new UnsupportedOperationException("Unexpected element type: " + elementType);
        SpoofaxElementType spoofaxElementType = (SpoofaxElementType)elementType;
        return spoofaxElementType.createElement(node);
    }

}
