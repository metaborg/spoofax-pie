package mb.pipe.run.spoofax.sdf;

import java.util.Collection;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.IToken;

import mb.pipe.run.core.model.IMsg;

public class ParseOutput implements IParseOutput {
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


    @Override public void accept(IParseOutputVisitor visitor) {
        if(ast == null || tokenStream == null) {
            visitor.failed(messages);
        } else if(recovered) {
            visitor.recovered(ast, tokenStream, messages);
        } else {
            visitor.success(ast, tokenStream, messages);
        }
    }
}
