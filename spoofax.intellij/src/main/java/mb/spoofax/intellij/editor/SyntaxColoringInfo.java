package mb.spoofax.intellij.editor;

import java.util.List;
import java.util.Objects;


public final class SyntaxColoringInfo implements ISyntaxColoringInfo {

    private final List<IToken> tokens;

    public SyntaxColoringInfo(List<IToken> tokens) {
        this.tokens = tokens;
    }

    @Override
    public List<IToken> getTokens() {
        return tokens;
    }

    public boolean equals(Object other) {
        if (this == other) return true;
        return other instanceof  SyntaxColoringInfo
            && equals((SyntaxColoringInfo)other);
    }

    public boolean equals(SyntaxColoringInfo other) {
        return other != null
            && this.tokens.equals(other.tokens);
    }

    public int hashCode() {
        return Objects.hash(this.tokens);
    }

}
