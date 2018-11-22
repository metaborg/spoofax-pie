package mb.spoofax.runtime.jsglr;

import mb.fs.java.JavaFSPath;
import mb.spoofax.api.SpoofaxRunEx;
import mb.spoofax.api.message.Message;
import mb.spoofax.api.message.MessageUtils;
import mb.spoofax.api.parse.Token;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.*;
import org.spoofax.jsglr.client.imploder.*;
import org.spoofax.jsglr.io.SGLR;
import org.spoofax.jsglr.shared.SGLRException;
import org.spoofax.terms.attachments.ParentTermFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;

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


    public ParseOutput parse(String text, String startSymbol, @Nullable JavaFSPath path) throws InterruptedException {
        final String pathStr = path != null ? path.toString() : "file-not-set-during-parsing";
        try {
            final SGLRParseResult result = parser.parse(text, pathStr, startSymbol);
            final @Nullable IStrategoTerm ast = (IStrategoTerm) result.output;
            if(ast == null) {
                throw new SpoofaxRunEx("Parser returned null AST even though parsing did not fail");
            }

            if(path != null) {
                SourcePathAttachment.setPathForTerm(ast, path);
            }

            final ArrayList<Token> tokenStream = TokenExtractor.extract(ast);

            final ParserErrorHandler errorHandler = new ParserErrorHandler(true, false, parser.getCollectedErrors());
            errorHandler.gatherNonFatalErrors(ast);
            final ArrayList<Message> messages = errorHandler.messages();
            final boolean recovered = MessageUtils.containsError(messages);

            return new ParseOutput(recovered, ast, tokenStream, messages);
        } catch(SGLRException e) {
            final ParserErrorHandler errorHandler = new ParserErrorHandler(true, true, parser.getCollectedErrors());
            errorHandler.processFatalException(new NullTokenizer(text, pathStr), e);
            final ArrayList<Message> messages = errorHandler.messages();
            return new ParseOutput(false, null, null, messages);
        }
    }
}
