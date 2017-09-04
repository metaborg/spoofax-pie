package mb.pipe.run.core.model.style;

import mb.pipe.run.core.model.parse.Token;

public class TokenStyleImpl implements TokenStyle {
    private static final long serialVersionUID = 1L;

    private final Token token;
    private final Style style;


    public TokenStyleImpl(Token token, Style style) {
        this.token = token;
        this.style = style;
    }


    @Override public Token token() {
        return token;
    }

    @Override public Style style() {
        return style;
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + style.hashCode();
        result = prime * result + token.hashCode();
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final TokenStyleImpl other = (TokenStyleImpl) obj;
        if(!style.equals(other.style))
            return false;
        if(!token.equals(other.token))
            return false;
        return true;
    }

    @Override public String toString() {
        return "TokenStyle(token: " + token + ", style: " + style + ")";
    }
}
