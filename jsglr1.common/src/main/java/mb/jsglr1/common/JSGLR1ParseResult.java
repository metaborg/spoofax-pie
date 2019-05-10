package mb.jsglr1.common;

import mb.common.message.Message;
import mb.common.message.Messages;
import mb.common.token.Token;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class JSGLR1ParseResult implements Serializable {
    public final boolean recovered;
    public final @Nullable IStrategoTerm ast;
    public final @Nullable ArrayList<Token> tokens;
    public final Messages messages;

    public JSGLR1ParseResult(boolean recovered, @Nullable IStrategoTerm ast, @Nullable ArrayList<Token> tokens, Messages messages) {
        this.recovered = recovered;
        this.ast = ast;
        this.tokens = tokens;
        this.messages = messages;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final JSGLR1ParseResult that = (JSGLR1ParseResult) o;
        if(recovered != that.recovered) return false;
        if(!Objects.equals(ast, that.ast)) return false;
        if(!Objects.equals(tokens, that.tokens)) return false;
        return messages.equals(that.messages);
    }

    @Override public int hashCode() {
        int result = (recovered ? 1 : 0);
        result = 31 * result + (ast != null ? ast.hashCode() : 0);
        result = 31 * result + (tokens != null ? tokens.hashCode() : 0);
        result = 31 * result + messages.hashCode();
        return result;
    }

    @Override public String toString() {
        return "JSGLR1ParseResult{" +
            "recovered=" + recovered +
            ", ast=" + ast +
            ", tokens=" + tokens +
            ", messages=" + messages +
            '}';
    }
}
