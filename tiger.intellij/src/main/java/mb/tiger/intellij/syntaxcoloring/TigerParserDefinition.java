package mb.tiger.intellij.syntaxcoloring;

import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.psi.tree.IFileElementType;
import mb.spoofax.intellij.editor.SpoofaxParserDefinition;
import mb.spoofax.intellij.editor.SpoofaxTokenTypeManager;

public class TigerParserDefinition extends SpoofaxParserDefinition {
    public TigerParserDefinition(LanguageFileType fileType, IFileElementType fileElementType, SpoofaxTokenTypeManager tokenTypeManager) {
        super(fileType, fileElementType, tokenTypeManager);
    }
}
