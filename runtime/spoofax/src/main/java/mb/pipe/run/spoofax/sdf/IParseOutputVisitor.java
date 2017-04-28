package mb.pipe.run.spoofax.sdf;

import java.util.Collection;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.IToken;

import mb.pipe.run.core.model.IMsg;

public interface IParseOutputVisitor {
    void success(IStrategoTerm ast, Collection<IToken> tokenStream, Collection<IMsg> messages);

    void recovered(IStrategoTerm ast, Collection<IToken> tokenStream, Collection<IMsg> messages);

    void failed(Collection<IMsg> messages);
}
