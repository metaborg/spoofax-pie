package mb.tiger.intellij;

import com.google.inject.Singleton;
import com.intellij.lang.ASTFactory;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import org.jetbrains.annotations.NotNull;

@Singleton
public final class TigerTokenTypes {
    private TigerTokenTypes() {}

    public static final IElementType TIGER_FILE = new IFileElementType("TIGER_FILE", TigerLanguage.instance) {
        @Override public ASTNode parseContents(@NotNull ASTNode chameleon) {
            return ASTFactory.leaf(TIGER_TEXT, chameleon.getChars());
        }
    };

    public static final IElementType TIGER_TEXT = new IElementType("TIGER_TEXT", TigerLanguage.instance);
}
