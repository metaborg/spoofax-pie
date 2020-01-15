package mb.jsglr1.common;

import mb.common.message.Messages;
import mb.common.token.Token;
import mb.common.util.ADT;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Optional;

@ADT
public abstract class JSGLR1ParseResult implements Serializable {
    public interface Cases<R> {
        R success(IStrategoTerm ast, ArrayList<? extends Token<IStrategoTerm>> tokens, Messages messages);

        R recovered(IStrategoTerm ast, ArrayList<? extends Token<IStrategoTerm>> tokens, Messages messages);

        R failed(Messages messages);
    }

    public Optional<IStrategoTerm> getAst() {
        return JSGLR1ParseResults.getAst(this);
    }

    public Optional<ArrayList<? extends Token<IStrategoTerm>>> getTokens() {
        return JSGLR1ParseResults.getTokens(this);
    }

    public Messages getMessages() {
        return JSGLR1ParseResults.getMessages(this);
    }

    public boolean hasSucceeded() {
        return JSGLR1ParseResults.caseOf(this).success_(true).otherwise_(false);
    }

    public boolean hasRecovered() {
        return JSGLR1ParseResults.caseOf(this).recovered_(true).otherwise_(false);
    }

    public boolean hasFailed() {
        return JSGLR1ParseResults.caseOf(this).failed_(true).otherwise_(false);
    }


    public abstract <R> R match(Cases<R> cases);

    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
