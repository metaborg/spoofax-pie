package mb.jsglr2.common;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Severity;
import mb.common.region.Region;
import mb.common.token.Token;
import mb.jsglr.common.FragmentedOriginLocationFixer;
import mb.jsglr.common.JSGLRTokens;
import mb.jsglr.common.JsglrParseException;
import mb.jsglr.common.JsglrParseInput;
import mb.jsglr.common.JsglrParseOutput;
import mb.jsglr.common.ResourceKeyAttachment;
import mb.jsglr.common.TokenUtil;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ITokens;
import org.spoofax.jsglr2.JSGLR2;
import org.spoofax.jsglr2.JSGLR2Implementation;
import org.spoofax.jsglr2.JSGLR2Request;
import org.spoofax.jsglr2.JSGLR2Variant;
import org.spoofax.jsglr2.imploder.IImplodeResult;
import org.spoofax.jsglr2.messages.Message;
import org.spoofax.jsglr2.parseforest.IParseForest;
import org.spoofax.jsglr2.parser.result.ParseResult;
import org.spoofax.jsglr2.parser.result.ParseSuccess;
import org.spoofax.jsglr2.recovery.IRecoveryParseState;
import org.spoofax.terms.attachments.ParentAttachment;

import java.util.ArrayList;
import java.util.Collection;

public class Jsglr2Parser {
    private final JSGLR2Implementation<IParseForest, Object, ?, IStrategoTerm, ?, ITokens> jsglr2;

    public Jsglr2Parser(JSGLR2Implementation<IParseForest, Object, ?, IStrategoTerm, ?, ITokens> jsglr2) {
        this.jsglr2 = jsglr2;
    }

    public Jsglr2Parser(Jsglr2ParseTable parseTable, JSGLR2Variant.Preset preset) {
        final JSGLR2<IStrategoTerm> jsglr2 = preset.getJSGLR2(parseTable.parseTable);
        if(!(jsglr2 instanceof JSGLR2Implementation)) {
            throw new IllegalStateException("Cannot create JSGLR2 parser, JSGLR2 implementation '" + jsglr2 + "' does not implement JSGLR2Implementation");
        }
        this.jsglr2 = (JSGLR2Implementation<IParseForest, Object, ?, IStrategoTerm, ?, ITokens>)jsglr2;
    }

    public Jsglr2Parser(Jsglr2ParseTable parseTable) {
        this(parseTable, JSGLR2Variant.Preset.recovery);
    }

    public JsglrParseOutput parse(JsglrParseInput input) throws JsglrParseException {
        final JSGLR2Request request = new JSGLR2Request(input.text.toString(), input.fileHint != null ? input.fileHint.toString() : "", input.startSymbol)
            .withAmbiguitiesReporting(true);
        final ParseResult<?> parseResult = jsglr2.parser.parse(request);
        if(!parseResult.isSuccess()) {
            final KeyedMessages messages = collectMessages(parseResult.messages, input.fileHint, input.rootDirectoryHint);
            throw JsglrParseException.parseFail(messages, input.startSymbol, input.fileHint, input.rootDirectoryHint);
        }
        final IParseForest parseForest = ((ParseSuccess<?>)parseResult).parseResult;
        final IImplodeResult<Object, ?, IStrategoTerm> implodeResult = jsglr2.imploder.implode(request, parseForest);
        final IStrategoTerm ast = implodeResult.ast();
        if(input.fileHint != null) {
            ParentAttachment.setParentAttachments(ast);
            ResourceKeyAttachment.setResourceKey(ast, input.fileHint);
        }

        final ITokens tokenStream = jsglr2.tokenizer.tokenize(request, implodeResult.intermediateResult()).tokens;
        parseResult.postProcessMessages(tokenStream);

        final FragmentedOriginLocationFixer.Result fixResult = FragmentedOriginLocationFixer.fixOriginLocations(
            input.text,
            ast,
            tokenStream,
            collectMessages(parseResult.messages, input.fileHint, input.rootDirectoryHint)
        );

        final JSGLRTokens tokens = convertTokens(fixResult.tokens);

        final boolean recovered;
        if(parseResult.parseState instanceof IRecoveryParseState) {
            recovered = ((IRecoveryParseState<?, ?, ?>)parseResult.parseState).appliedRecovery();
        } else {
            recovered = false;
        }
        final boolean ambiguous = implodeResult.isAmbiguous();

        return new JsglrParseOutput(fixResult.ast, tokens, fixResult.messages, recovered, ambiguous, input.startSymbol, input.fileHint, input.rootDirectoryHint);
    }

    private static KeyedMessages collectMessages(Collection<Message> messages, @Nullable ResourceKey fileHint, @Nullable ResourcePath rootDirectoryHint) {
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();
        for(Message message : messages) {
            final @Nullable Region region;
            if(message.region != null) {
                region = Region.fromOffsets(message.region.startOffset, message.region.endOffset, message.region.startRow, message.region.endRow);
            } else {
                region = null;
            }
            final Severity severity;
            switch(message.severity) {
                case WARNING:
                    severity = Severity.Warning;
                    break;
                case ERROR:
                default:
                    severity = Severity.Error;
                    break;
            }
            messagesBuilder.addMessage(message.message, severity, fileHint, region);
        }
        return messagesBuilder.build(fileHint != null ? fileHint : rootDirectoryHint);
    }

    private static JSGLRTokens convertTokens(ITokens tokens) {
        final ArrayList<Token<IStrategoTerm>> tokenStream = new ArrayList<>(tokens.getTokenCount());
        for(IToken token : tokens) {
            if(token.getStartOffset() < 0 || token.getEndOffset() < 0) continue; // Skip special start/end tokens.
            tokenStream.add(TokenUtil.convertToken(token));
        }
        return new JSGLRTokens(tokenStream);
    }
}
