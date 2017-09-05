package mb.spoofax.runtime.impl.sdf;

import java.util.ArrayList;

import javax.annotation.Nullable;

import mb.spoofax.runtime.model.parse.Token;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.Disambiguator;
import org.spoofax.jsglr.client.ParseTable;
import org.spoofax.jsglr.client.SGLRParseResult;
import org.spoofax.jsglr.client.imploder.NullTokenizer;
import org.spoofax.jsglr.client.imploder.TermTreeFactory;
import org.spoofax.jsglr.client.imploder.TreeBuilder;
import org.spoofax.jsglr.io.SGLR;
import org.spoofax.jsglr.shared.SGLRException;
import org.spoofax.terms.attachments.ParentTermFactory;

import mb.spoofax.runtime.model.SpoofaxRunEx;
import mb.spoofax.runtime.model.message.Msg;
import mb.spoofax.runtime.model.message.Msgs;

public class Parser {
    private final SGLR parser;
    private final Disambiguator disambiguator;


    public Parser(ParseTable parseTable, ITermFactory termFactory) {
        final TermTreeFactory treeFactory = new TermTreeFactory(new ParentTermFactory(termFactory));
        final TreeBuilder treeBuilder = new TreeBuilder(treeFactory);

        this.parser = new SGLR(treeBuilder, parseTable);
        this.parser.setUseStructureRecovery(true);
        this.parser.setTimeout(5000);
        this.parser.setDisambiguatorTimeout(5000);
        this.parser.setApplyCompletionProd(false);
        this.parser.setNewCompletionMode(false);

        this.disambiguator = parser.getDisambiguator();
        this.disambiguator.setHeuristicFilters(false);
    }


    public ParseOutput parse(String text, String startSymbol) throws InterruptedException {
        try {
            final SGLRParseResult result = parser.parse(text, "file", startSymbol);
            final @Nullable IStrategoTerm ast = (IStrategoTerm) result.output;
            if(ast == null) {
                throw new SpoofaxRunEx("Parser returned null AST even though parsing did not fail");
            }

            final ArrayList<Token> tokenStream = TokenExtractor.extract(ast);

            final ParserErrorHandler errorHandler = new ParserErrorHandler(true, false, parser.getCollectedErrors());
            errorHandler.gatherNonFatalErrors(ast);
            final ArrayList<Msg> messages = errorHandler.messages();
            boolean recovered = Msgs.containsErrors(messages);

            return new ParseOutput(recovered, ast, tokenStream, messages);
        } catch(SGLRException e) {
            final ParserErrorHandler errorHandler = new ParserErrorHandler(true, true, parser.getCollectedErrors());
            errorHandler.processFatalException(new NullTokenizer(text, "file"), e);
            final ArrayList<Msg> messages = errorHandler.messages();
            return new ParseOutput(false, null, null, messages);
        }
    }
}
