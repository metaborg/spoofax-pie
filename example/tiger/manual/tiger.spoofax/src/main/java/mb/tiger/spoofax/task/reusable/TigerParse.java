package mb.tiger.spoofax.task.reusable;

import mb.common.message.Message;
import mb.common.message.Messages;
import mb.common.token.Token;
import mb.jsglr1.common.JSGLR1ParseResult;
import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.Provider;
import mb.pie.api.ResourceStringProvider;
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
import java.util.function.Function;

@LanguageScope
public class TigerParse implements TaskDef<Provider<String>, JSGLR1ParseResult> {
    private final javax.inject.Provider<TigerParser> parserProvider;

    @Inject
    public TigerParse(javax.inject.Provider<TigerParser> parserProvider) {
        this.parserProvider = parserProvider;
    }

    @Override public String getId() {
        return "mb.tiger.spoofax.task.TigerParse";
    }

    @Override
    public JSGLR1ParseResult exec(ExecContext context, Provider<String> stringProvider) throws InterruptedException {
        final String text;
        try {
            text = context.require(stringProvider);
        } catch(ExecException | IOException e) {
            return JSGLR1ParseResult.failed(Messages.of(new Message("Cannot get text input for parser from '" + stringProvider + "'", e)));
        }
        final TigerParser parser = parserProvider.get();
        return parser.parse(text, "Module");
    }


    public Provider<@Nullable IStrategoTerm> createAstProvider(Provider<String> stringProvider) {
        return this.createSerializableTask(stringProvider).map(new AstMapper());
    }

    public Provider<@Nullable IStrategoTerm> createAstProvider(ResourceKey key) {
        return this.createSerializableTask(new ResourceStringProvider(key)).map(new AstMapper());
    }

    public Provider<@Nullable IStrategoTerm> createAstProvider(ResourceKey key, ResourceStamper<ReadableResource> stamper) {
        return this.createSerializableTask(new ResourceStringProvider(key, stamper)).map(new AstMapper());
    }

    public Provider<@Nullable IStrategoTerm> createAstProvider(ResourceKey key, ResourceStamper<ReadableResource> stamper, Charset charset) {
        return this.createSerializableTask(new ResourceStringProvider(key, stamper, charset)).map(new AstMapper());
    }


    public Provider<@Nullable ArrayList<? extends Token<IStrategoTerm>>> createTokensProvider(Provider<String> stringProvider) {
        return this.createSerializableTask(stringProvider).map(new TokensMapper());
    }

    public Provider<@Nullable ArrayList<? extends Token<IStrategoTerm>>> createTokensProvider(ResourceKey key) {
        return this.createSerializableTask(new ResourceStringProvider(key)).map(new TokensMapper());
    }

    public Provider<@Nullable ArrayList<? extends Token<IStrategoTerm>>> createTokensProvider(ResourceKey key, ResourceStamper<ReadableResource> stamper) {
        return this.createSerializableTask(new ResourceStringProvider(key, stamper)).map(new TokensMapper());
    }

    public Provider<@Nullable ArrayList<? extends Token<IStrategoTerm>>> createTokensProvider(ResourceKey key, ResourceStamper<ReadableResource> stamper, Charset charset) {
        return this.createSerializableTask(new ResourceStringProvider(key, stamper, charset)).map(new TokensMapper());
    }
}

class AstMapper implements Function<JSGLR1ParseResult, @Nullable IStrategoTerm>, Serializable {
    @Override public @Nullable IStrategoTerm apply(JSGLR1ParseResult result) {
        return result.getAst().orElse(null);
    }
}

class TokensMapper implements Function<JSGLR1ParseResult, @Nullable ArrayList<? extends Token<IStrategoTerm>>>, Serializable {
    @Override public @Nullable ArrayList<? extends Token<IStrategoTerm>> apply(JSGLR1ParseResult result) {
        return result.getTokens().orElse(null);
    }
}
