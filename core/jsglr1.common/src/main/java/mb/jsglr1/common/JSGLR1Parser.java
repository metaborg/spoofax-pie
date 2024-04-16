package mb.jsglr1.common;

import mb.common.message.KeyedMessages;
import mb.common.message.Messages;
import mb.jsglr.common.FragmentedOriginLocationFixer;
import mb.jsglr.common.JSGLRTokens;
import mb.jsglr.common.JsglrParseException;
import mb.jsglr.common.JsglrParseInput;
import mb.jsglr.common.JsglrParseOutput;
import mb.jsglr.common.ResourceKeyAttachment;
import mb.jsglr.common.TokenUtil;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.Disambiguator;
import org.spoofax.jsglr.client.SGLRParseResult;
import mb.jsglr.shared.ImploderAttachment;
import org.spoofax.jsglr.client.imploder.ImploderOriginTermFactory;
import mb.jsglr.shared.NullTokenizer;
import org.spoofax.jsglr.client.imploder.TermTreeFactory;
import org.spoofax.jsglr.client.imploder.TreeBuilder;
import org.spoofax.jsglr.io.SGLR;
import org.spoofax.jsglr.shared.SGLRException;
import org.spoofax.terms.TermFactory;
import org.spoofax.terms.attachments.ParentTermFactory;

public class JSGLR1Parser {
    private final SGLR parser;

    public JSGLR1Parser(JSGLR1ParseTable parseTable) {
        this(parseTable, new ImploderOriginTermFactory(new TermFactory()));
    }

    public JSGLR1Parser(JSGLR1ParseTable parseTable, ITermFactory termFactory) {
        final TermTreeFactory treeFactory = new TermTreeFactory(new ParentTermFactory(termFactory));
        final TreeBuilder treeBuilder = new TreeBuilder(treeFactory);

        this.parser = new SGLR(treeBuilder, parseTable.internalParseTable);
        this.parser.setUseStructureRecovery(true);
        this.parser.setApplyCompletionProd(false);
        this.parser.setNewCompletionMode(false);

        final Disambiguator disambiguator = parser.getDisambiguator();
        disambiguator.setHeuristicFilters(false);
    }

    public JsglrParseOutput parse(JsglrParseInput input) throws JsglrParseException, InterruptedException {
        try {
            final SGLRParseResult result = parser.parse(input.text.toString(), input.fileHint != null ? input.fileHint.toString() : null, input.startSymbol);
            if(result.output == null) {
                throw new RuntimeException("BUG: parser returned null output even though parsing did not fail");
            }
            if(!(result.output instanceof IStrategoTerm)) {
                throw new RuntimeException("BUG: parser returned an output that is not an instance of IStrategoTerm");
            }

            final IStrategoTerm ast = (IStrategoTerm)result.output;
            if(input.fileHint != null) {
                ResourceKeyAttachment.setResourceKey(ast, input.fileHint);
            }

            final MessagesUtil messagesUtil = new MessagesUtil(true, false, parser.getCollectedErrors());
            messagesUtil.gatherNonFatalErrors(ast);

            final FragmentedOriginLocationFixer.Result fixResult = FragmentedOriginLocationFixer.fixOriginLocations(
                input.text,
                ast,
                ImploderAttachment.get(ast).getLeftToken().getTokenizer(),
                toMessages(messagesUtil.getMessages(), input.fileHint)
            );

            final boolean ambiguous = parser.getAmbiguitiesCount() > 0;
            final JSGLRTokens tokens = TokenUtil.extract(fixResult.tokens, ambiguous);

            final boolean recovered = fixResult.messages.containsError();

            return new JsglrParseOutput(fixResult.ast, tokens, fixResult.messages, recovered, ambiguous, input.startSymbol, input.fileHint, input.rootDirectoryHint);
        } catch(SGLRException e) {
            final MessagesUtil messagesUtil = new MessagesUtil(true, true, parser.getCollectedErrors());
            messagesUtil.processFatalException(new NullTokenizer(input.text.toString(), null), e);
            final KeyedMessages messages = toMessages(messagesUtil.getMessages(), input.fileHint);
            throw JsglrParseException.parseFail(messages, input.startSymbol, input.fileHint, input.rootDirectoryHint);
        }
    }

    private static KeyedMessages toMessages(Messages messages, @Nullable ResourceKey fileHint) {
        if(fileHint != null) {
            return messages.toKeyed(fileHint);
        } else {
            return messages.toKeyed();
        }
    }
}
