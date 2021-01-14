package mb.common.style;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;

public class StylingImpl implements Styling {
    private final ArrayList<TokenStyle> stylePerToken;


    public StylingImpl(ArrayList<TokenStyle> stylePerToken) {
        this.stylePerToken = stylePerToken;
    }


    @Override public ArrayList<TokenStyle> getStylePerToken() {
        return stylePerToken;
    }


    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final StylingImpl styling = (StylingImpl) o;
        return stylePerToken.equals(styling.stylePerToken);
    }

    @Override public int hashCode() {
        return stylePerToken.hashCode();
    }

    @Override public String toString() {
        return "StylingImpl(" + stylePerToken + ')';
    }
}
