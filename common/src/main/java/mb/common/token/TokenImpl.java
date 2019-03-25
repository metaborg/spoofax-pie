package mb.common.token;

import mb.common.region.Region;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.util.Objects;

public class TokenImpl implements Token {
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

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final TokenImpl token = (TokenImpl) o;
        if(!region.equals(token.region)) return false;
        if(!type.equals(token.type)) return false;
        return Objects.equals(associatedTerm, token.associatedTerm);
    }

    @Override public int hashCode() {
        int result = region.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + (associatedTerm != null ? associatedTerm.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return "Token(region: " + region + ", type: " + type + ")";
    }
}
