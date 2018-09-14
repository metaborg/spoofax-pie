package mb.spoofax.runtime.jsglr;

import java.util.ArrayList;

import javax.annotation.Nullable;

import mb.spoofax.api.message.Message;
import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.spoofax.api.parse.Token;

public class ParseOutput {
    public final boolean recovered;
    public final @Nullable IStrategoTerm ast;
    public final @Nullable ArrayList<Token> tokenStream;
    public final ArrayList<Message> messages;


    public ParseOutput(boolean recovered, @Nullable IStrategoTerm ast, @Nullable ArrayList<Token> tokenStream,
        ArrayList<Message> messages) {
        this.recovered = recovered;
        this.ast = ast;
        this.tokenStream = tokenStream;
        this.messages = messages;
    }
}
