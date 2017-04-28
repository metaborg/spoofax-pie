package mb.pipe.run.core.model.parse;

import mb.pipe.run.core.model.region.IRegion;

public class Token implements IToken {
    private static final long serialVersionUID = 1L;

    private final IRegion region;
    private final ITokenType type;
    private final String text;


    public Token(IRegion region, ITokenType type, String text) {
        this.region = region;
        this.type = type;
        this.text = text;
    }


    @Override public IRegion region() {
        return region;
    }

    @Override public ITokenType type() {
        return type;
    }

    @Override public String text() {
        return text;
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + region.hashCode();
        result = prime * result + type.hashCode();
        result = prime * result + text.hashCode();
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final Token other = (Token) obj;
        if(!region.equals(other.region))
            return false;
        if(!type.equals(other.type))
            return false;
        if(!text.equals(other.text))
            return false;
        return true;
    }

    @Override public String toString() {
        return text;
    }
}
