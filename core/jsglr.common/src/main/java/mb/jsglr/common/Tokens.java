package mb.jsglr.common;

import mb.common.token.Token;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.Serializable;
import java.util.ArrayList;

public class Tokens implements Serializable {
    public final ArrayList<? extends Token<IStrategoTerm>> tokens;

    public Tokens(ArrayList<? extends Token<IStrategoTerm>> tokens) {
        this.tokens = tokens;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final Tokens that = (Tokens)o;
        return tokens.equals(that.tokens);
    }

    @Override public int hashCode() {
        return tokens.hashCode();
    }

    @Override public String toString() {
        return "Tokens(" + tokens + ")";
    }
}
