package mb.jsglr1.common;

import mb.common.message.Message;
import mb.common.token.Token;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class JSGLR1ParseOutput implements Serializable {
    public final boolean recovered;
    public final @Nullable IStrategoTerm ast;
    public final @Nullable ArrayList<Token> tokens;
    public final ArrayList<Message> messages;

    public JSGLR1ParseOutput(boolean recovered, @Nullable IStrategoTerm ast, @Nullable ArrayList<Token> tokens, ArrayList<Message> messages) {
        this.recovered = recovered;
        this.ast = ast;
        this.tokens = tokens;
        this.messages = messages;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final JSGLR1ParseOutput that = (JSGLR1ParseOutput) o;
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
        return "ParseOutput{" +
            "recovered=" + recovered +
            ", ast=" + ast +
            ", tokens=" + tokens +
            ", messages=" + messages +
            '}';
    }
}
