package mb.common.style;

import mb.common.token.Token;

public class TokenStyleImpl implements TokenStyle {
    private final Token token;
    private final Style style;


    public TokenStyleImpl(Token token, Style style) {
        this.token = token;
        this.style = style;
    }


    @Override public Token getToken() {
        return token;
    }

    @Override public Style getStyle() {
        return style;
    }


    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final TokenStyleImpl that = (TokenStyleImpl) o;
        if(!token.equals(that.token)) return false;
        return style.equals(that.style);
    }

    @Override public int hashCode() {
        int result = token.hashCode();
        result = 31 * result + style.hashCode();
        return result;
    }

    @Override public String toString() {
        return "TokenStyle(token: " + token + ", style: " + style + ")";
    }
}
