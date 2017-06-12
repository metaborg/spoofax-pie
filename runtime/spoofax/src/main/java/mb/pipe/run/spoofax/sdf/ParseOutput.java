package mb.pipe.run.spoofax.sdf;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.pipe.run.core.model.message.Msg;
import mb.pipe.run.core.model.parse.Token;

public class ParseOutput {
    public final boolean recovered;
    public final @Nullable IStrategoTerm ast;
    public final @Nullable List<Token> tokenStream;
    public final Collection<Msg> messages;


    public ParseOutput(boolean recovered, @Nullable IStrategoTerm ast, @Nullable List<Token> tokenStream,
        Collection<Msg> messages) {
        this.recovered = recovered;
        this.ast = ast;
        this.tokenStream = tokenStream;
        this.messages = messages;
    }
}
