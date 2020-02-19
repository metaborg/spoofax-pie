package mb.tiger.spoofax.task.reusable;

import mb.common.message.Message;
import mb.common.message.Messages;
import mb.common.token.Token;
import mb.jsglr1.common.JSGLR1ParseResult;
import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.Function;
import mb.pie.api.ResourceStringSupplier;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.ResourceStamper;
import mb.resource.ReadableResource;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.LanguageScope;
import mb.tiger.TigerParser;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;

@LanguageScope
public class TigerParse implements TaskDef<Supplier<String>, JSGLR1ParseResult> {
    private final javax.inject.Provider<TigerParser> parserProvider;

    @Inject public TigerParse(javax.inject.Provider<TigerParser> parserProvider) {
        this.parserProvider = parserProvider;
    }

    @Override public String getId() {
        return "mb.tiger.spoofax.task.reusable.TigerParse";
    }

    @Override public JSGLR1ParseResult exec(
        ExecContext context,
        Supplier<String> stringSupplier
    ) throws InterruptedException {
        final String text;
        try {
            text = context.require(stringSupplier);
        } catch(ExecException | IOException e) {
            return JSGLR1ParseResult.failed(Messages.of(new Message("Cannot get text input for parser from '" + stringSupplier + "'", e)));
        }
        final TigerParser parser = parserProvider.get();
        return parser.parse(text, "Module");
    }


    public Supplier<@Nullable IStrategoTerm> createAstSupplier(Supplier<String> stringSupplier) {
        return this.createSupplier(stringSupplier).map(new AstMapper());
    }

    public Supplier<@Nullable IStrategoTerm> createAstSupplier(ResourceKey key) {
        return this.createSupplier(new ResourceStringSupplier(key)).map(new AstMapper());
    }

    public Supplier<@Nullable IStrategoTerm> createAstSupplier(ResourceKey key, ResourceStamper<ReadableResource> stamper) {
        return this.createSupplier(new ResourceStringSupplier(key, stamper)).map(new AstMapper());
    }

    public Supplier<@Nullable IStrategoTerm> createAstSupplier(ResourceKey key, ResourceStamper<ReadableResource> stamper, Charset charset) {
        return this.createSupplier(new ResourceStringSupplier(key, stamper, charset)).map(new AstMapper());
    }

    public Supplier<@Nullable ArrayList<? extends Token<IStrategoTerm>>> createTokensSupplier(Supplier<String> stringSupplier) {
        return this.createSupplier(stringSupplier).map(new TokensMapper());
    }

    public Supplier<@Nullable ArrayList<? extends Token<IStrategoTerm>>> createTokensSupplier(ResourceKey key) {
        return this.createSupplier(new ResourceStringSupplier(key)).map(new TokensMapper());
    }

    public Supplier<@Nullable ArrayList<? extends Token<IStrategoTerm>>> createTokensSupplier(ResourceKey key, ResourceStamper<ReadableResource> stamper) {
        return this.createSupplier(new ResourceStringSupplier(key, stamper)).map(new TokensMapper());
    }

    public Supplier<@Nullable ArrayList<? extends Token<IStrategoTerm>>> createTokensSupplier(ResourceKey key, ResourceStamper<ReadableResource> stamper, Charset charset) {
        return this.createSupplier(new ResourceStringSupplier(key, stamper, charset)).map(new TokensMapper());
    }


    public Supplier<Messages> createMessagesSupplier(Supplier<String> stringSupplier) {
        return this.createSupplier(stringSupplier).map(new MessagesMapper());
    }

    public Supplier<Messages> createMessagesSupplier(ResourceKey key) {
        return this.createSupplier(new ResourceStringSupplier(key)).map(new MessagesMapper());
    }

    public Supplier<Messages> createMessagesSupplier(ResourceKey key, ResourceStamper<ReadableResource> stamper) {
        return this.createSupplier(new ResourceStringSupplier(key, stamper)).map(new MessagesMapper());
    }

    public Supplier<Messages> createMessagesSupplier(ResourceKey key, ResourceStamper<ReadableResource> stamper, Charset charset) {
        return this.createSupplier(new ResourceStringSupplier(key, stamper, charset)).map(new MessagesMapper());
    }


    public Function<Supplier<String>, @Nullable IStrategoTerm> createAstFunction() {
        return this.createFunction().mapOutput(new AstMapper());
    }

    public Function<Supplier<String>, @Nullable ArrayList<? extends Token<IStrategoTerm>>> createTokensFunction() {
        return this.createFunction().mapOutput(new TokensMapper());
    }

    public Function<Supplier<String>, Messages> createMessagesFunction() {
        return this.createFunction().mapOutput(new MessagesMapper());
    }
}

class AstMapper implements java.util.function.Function<JSGLR1ParseResult, @Nullable IStrategoTerm>, Serializable {
    @Override public @Nullable IStrategoTerm apply(JSGLR1ParseResult result) {
        return result.getAst().orElse(null);
    }
}

class TokensMapper implements java.util.function.Function<JSGLR1ParseResult, @Nullable ArrayList<? extends Token<IStrategoTerm>>>, Serializable {
    @Override public @Nullable ArrayList<? extends Token<IStrategoTerm>> apply(JSGLR1ParseResult result) {
        return result.getTokens().orElse(null);
    }
}

class MessagesMapper implements java.util.function.Function<JSGLR1ParseResult, Messages>, Serializable {
    @Override public Messages apply(JSGLR1ParseResult result) {
        return result.getMessages();
    }
}
