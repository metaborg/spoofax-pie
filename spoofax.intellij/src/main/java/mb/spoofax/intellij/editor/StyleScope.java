package mb.spoofax.intellij.editor;

import com.intellij.openapi.editor.colors.TextAttributesKey;

import java.util.Arrays;

public class StyleScope {
    public final String scope;
    public final TextAttributesKey[] styles;

    public StyleScope(String scope, TextAttributesKey... styles) {
        this.scope = scope;
        this.styles = styles;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final StyleScope that = (StyleScope) o;
        if(!scope.equals(that.scope)) return false;
        return Arrays.equals(styles, that.styles);
    }

    @Override public int hashCode() {
        int result = scope.hashCode();
        result = 31 * result + Arrays.hashCode(styles);
        return result;
    }

    @Override public String toString() {
        return scope;
    }
}
