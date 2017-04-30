package mb.pipe.run.core.model.style;

import mb.pipe.run.core.model.parse.IToken;

public class TokenStyle implements ITokenStyle {
    private static final long serialVersionUID = 1L;

    private final IToken token;
    private final IStyle style;


    public TokenStyle(IToken token, IStyle style) {
        this.token = token;
        this.style = style;
    }


    @Override public IToken token() {
        return token;
    }

    @Override public IStyle style() {
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
        final TokenStyle other = (TokenStyle) obj;
        if(!style.equals(other.style))
            return false;
        if(!token.equals(other.token))
            return false;
        return true;
    }
}
