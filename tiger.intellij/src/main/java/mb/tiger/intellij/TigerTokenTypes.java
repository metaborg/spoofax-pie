package mb.tiger.intellij;

import com.google.inject.Singleton;
import com.intellij.lang.ASTFactory;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;


@Singleton
public final class TigerTokenTypes {

    // Cannot be instantiated.
    private TigerTokenTypes() {}

    public static final IElementType TIGER_FILE = new IFileElementType("TIGER_FILE", TigerLanguage.INSTANCE) {
        @Nullable
        @Override
        public ASTNode parseContents(@NotNull ASTNode chameleon) {
            return ASTFactory.leaf(TIGER_TEXT, chameleon.getChars());
        }
    };

    public static final IElementType TIGER_TEXT = new IElementType("TIGER_TEXT", TigerLanguage.INSTANCE);
}
