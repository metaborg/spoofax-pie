package mb.pipe.run.spoofax.sdf;

import java.util.Collection;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.pipe.run.core.model.message.IMsg;
import mb.pipe.run.core.model.parse.IToken;

public class ParseOutput {
    public final boolean recovered;
    public final @Nullable IStrategoTerm ast;
    public final @Nullable Collection<IToken> tokenStream;
    public final Collection<IMsg> messages;


    public ParseOutput(boolean recovered, @Nullable IStrategoTerm ast, @Nullable Collection<IToken> tokenStream,
        Collection<IMsg> messages) {
        this.recovered = recovered;
        this.ast = ast;
        this.tokenStream = tokenStream;
        this.messages = messages;
    }
}
