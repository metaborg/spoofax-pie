package mb.pipe.run.spoofax.sdf;

import java.util.Collection;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.Disambiguator;
import org.spoofax.jsglr.client.ParseTable;
import org.spoofax.jsglr.client.SGLRParseResult;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ITokenizer;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.jsglr.client.imploder.NullTokenizer;
import org.spoofax.jsglr.client.imploder.TermTreeFactory;
import org.spoofax.jsglr.client.imploder.TreeBuilder;
import org.spoofax.jsglr.io.SGLR;
import org.spoofax.jsglr.shared.SGLRException;
import org.spoofax.terms.attachments.ParentTermFactory;

import com.google.common.collect.Lists;

import mb.pipe.run.core.PipeRunEx;
import mb.pipe.run.core.model.IMsg;
import mb.pipe.run.core.model.Msgs;

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
                throw new PipeRunEx("Parser returned null AST even though parsing did not fail");
            }

            final ImploderAttachment rootImploderAttachment = ImploderAttachment.get(ast);
            final ITokenizer tokenizer = rootImploderAttachment.getLeftToken().getTokenizer();
            final int tokenCount = tokenizer.getTokenCount();
            final Collection<IToken> tokenStream = Lists.newArrayListWithExpectedSize(tokenCount);
            for(int i = 0; i < tokenCount; ++i) {
                final IToken token = tokenizer.getTokenAt(i);
                tokenStream.add(token);
            }

            final ParserErrorHandler errorHandler = new ParserErrorHandler(true, false, parser.getCollectedErrors());
            errorHandler.gatherNonFatalErrors(ast);
            final Collection<IMsg> messages = errorHandler.messages();
            boolean recovered = Msgs.containsErrors(messages);

            return new ParseOutput(recovered, ast, tokenStream, messages);
        } catch(SGLRException e) {
            final ParserErrorHandler errorHandler = new ParserErrorHandler(true, true, parser.getCollectedErrors());
            errorHandler.processFatalException(new NullTokenizer(text, "file"), e);
            final Collection<IMsg> messages = errorHandler.messages();
            return new ParseOutput(false, null, null, messages);
        }
    }
}
