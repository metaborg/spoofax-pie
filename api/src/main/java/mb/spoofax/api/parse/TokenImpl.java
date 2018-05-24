package mb.spoofax.api.parse;

import javax.annotation.Nullable;

import mb.spoofax.api.region.Region;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class TokenImpl implements Token {
    private static final long serialVersionUID = 1L;

    private final Region region;
    private final TokenType type;
    private final @Nullable IStrategoTerm associatedTerm;


    public TokenImpl(Region region, TokenType type, @Nullable IStrategoTerm associatedTerm) {
        this.region = region;
        this.type = type;
        this.associatedTerm = associatedTerm;
    }


    @Override public Region region() {
        return region;
    }

    @Override public TokenType type() {
        return type;
    }

    @Override public @Nullable IStrategoTerm associatedTerm() {
        return associatedTerm;
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + region.hashCode();
        result = prime * result + type.hashCode();
        result = prime * result + ((associatedTerm == null) ? 0 : associatedTerm.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final TokenImpl other = (TokenImpl) obj;
        if(!region.equals(other.region))
            return false;
        if(!type.equals(other.type))
            return false;
        if(associatedTerm == null) {
            return other.associatedTerm == null;
        } else return associatedTerm.equals(other.associatedTerm);
    }

    @Override public String toString() {
        return "Token(region: " + region + ", type: " + type + ")";
    }
}
