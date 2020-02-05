package mb.spoofax.intellij.psi;

import com.intellij.lang.Language;
import com.intellij.psi.tree.IElementType;

public final class SpoofaxTokenType extends IElementType {
    private final String scope;

    public SpoofaxTokenType(String scope, Language language) {
        super(scope, language);
        this.scope = scope;
    }

    public String getScope() {
        return scope;
    }
}
