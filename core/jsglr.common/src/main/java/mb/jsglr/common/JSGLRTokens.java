package mb.jsglr.common;

import mb.common.token.Token;
import mb.common.token.Tokens;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.Serializable;
import java.util.ArrayList;

public class JSGLRTokens implements Tokens<IStrategoTerm>, Serializable {
    public final ArrayList<? extends Token<IStrategoTerm>> tokens;

    public JSGLRTokens(ArrayList<? extends Token<IStrategoTerm>> tokens) {
        this.tokens = tokens;
    }

    @Override public ArrayList<? extends Token<IStrategoTerm>> getTokens() {
        return tokens;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final JSGLRTokens that = (JSGLRTokens)o;
        return tokens.equals(that.tokens);
    }

    @Override public int hashCode() {
        return tokens.hashCode();
    }

    @Override public String toString() {
        return "Tokens(" + tokens + ")";
    }
}
