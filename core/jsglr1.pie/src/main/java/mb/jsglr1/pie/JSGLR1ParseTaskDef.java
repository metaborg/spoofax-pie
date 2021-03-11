package mb.jsglr1.pie;

import mb.common.message.KeyedMessages;
import mb.common.message.Messages;
import mb.common.result.Result;
import mb.jsglr.common.JSGLRTokens;
import mb.jsglr1.common.JSGLR1ParseException;
import mb.jsglr1.common.JSGLR1ParseOutput;
import mb.pie.api.ExecContext;
import mb.pie.api.Function;
import mb.pie.api.ResourceStringSupplier;
import mb.pie.api.SerializableFunction;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.ResourceStamper;
import mb.resource.ReadableResource;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.Serializable;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;

public abstract class JSGLR1ParseTaskDef implements TaskDef<Supplier<String>, Result<JSGLR1ParseOutput, JSGLR1ParseException>> {
    protected abstract Result<JSGLR1ParseOutput, JSGLR1ParseException> parse(ExecContext context, String text, @Nullable String startSymbol, @Nullable ResourceKey resource) throws Exception;

    @Override
    public Result<JSGLR1ParseOutput, JSGLR1ParseException> exec(ExecContext context, Supplier<String> stringSupplier) throws Exception {
        try {
            // TODO: make start symbol configurable
            // TODO: allow passing in a resource as a hint. Only try to get from supplier if no hint was passed.
            // TODO: like `Has(Optional)Messages`, create and use a `Has(Optional)ResourceKey` interface as well?
            final @Nullable ResourceKey resourceKey = tryGetResourceKeyFromStringSupplier(stringSupplier);
            return parse(context, context.require(stringSupplier), null, resourceKey);
        } catch(UncheckedIOException e) {
            return Result.ofErr(JSGLR1ParseException.readStringFail(stringSupplier.toString(), e.getCause()));
        }
    }


    public Supplier<Result<IStrategoTerm, JSGLR1ParseException>> createRecoverableAstSupplier(Supplier<String> stringSupplier) {
        return createSupplier(stringSupplier).map(RecoverableAstMapper.instance);
    }

    public Supplier<Result<IStrategoTerm, JSGLR1ParseException>> createRecoverableAstSupplier(ResourceKey key) {
        return createRecoverableAstSupplier(rsp(key));
    }

    public Supplier<Result<IStrategoTerm, JSGLR1ParseException>> createRecoverableAstSupplier(ResourceKey key, ResourceStamper<ReadableResource> stamper) {
        return createRecoverableAstSupplier(rsp(key, stamper));
    }

    public Supplier<Result<IStrategoTerm, JSGLR1ParseException>> createRecoverableAstSupplier(ResourceKey key, Charset charset) {
        return createRecoverableAstSupplier(rsp(key, charset));
    }

    public Supplier<Result<IStrategoTerm, JSGLR1ParseException>> createRecoverableAstSupplier(ResourceKey key, ResourceStamper<ReadableResource> stamper, Charset charset) {
        return createRecoverableAstSupplier(rsp(key, stamper, charset));
    }


    public Supplier<Result<IStrategoTerm, JSGLR1ParseException>> createAstSupplier(Supplier<String> stringSupplier) {
        return createSupplier(stringSupplier).map(AstMapper.instance); // TODO: pass an outputstamper that only does an equals check on the AST.
    }

    public Supplier<Result<IStrategoTerm, JSGLR1ParseException>> createAstSupplier(ResourceKey key) {
        return createAstSupplier(rsp(key));
    }

    public Supplier<Result<IStrategoTerm, JSGLR1ParseException>> createAstSupplier(ResourceKey key, ResourceStamper<ReadableResource> stamper) {
        return createAstSupplier(rsp(key, stamper));
    }

    public Supplier<Result<IStrategoTerm, JSGLR1ParseException>> createAstSupplier(ResourceKey key, Charset charset) {
        return createAstSupplier(rsp(key, charset));
    }

    public Supplier<Result<IStrategoTerm, JSGLR1ParseException>> createAstSupplier(ResourceKey key, ResourceStamper<ReadableResource> stamper, Charset charset) {
        return createAstSupplier(rsp(key, stamper, charset));
    }


    public Supplier<Result<JSGLRTokens, JSGLR1ParseException>> createRecoverableTokensSupplier(Supplier<String> stringSupplier) {
        return createSupplier(stringSupplier).map(RecoverableTokensMapper.instance);
    }

    public Supplier<Result<JSGLRTokens, JSGLR1ParseException>> createRecoverableTokensSupplier(ResourceKey key) {
        return createRecoverableTokensSupplier(rsp(key));
    }

    public Supplier<Result<JSGLRTokens, JSGLR1ParseException>> createRecoverableTokensSupplier(ResourceKey key, ResourceStamper<ReadableResource> stamper) {
        return createRecoverableTokensSupplier(rsp(key, stamper));
    }

    public Supplier<Result<JSGLRTokens, JSGLR1ParseException>> createRecoverableTokensSupplier(ResourceKey key, Charset charset) {
        return createRecoverableTokensSupplier(rsp(key, charset));
    }

    public Supplier<Result<JSGLRTokens, JSGLR1ParseException>> createRecoverableTokensSupplier(ResourceKey key, ResourceStamper<ReadableResource> stamper, Charset charset) {
        return createRecoverableTokensSupplier(rsp(key, stamper, charset));
    }


    public Supplier<Result<JSGLRTokens, JSGLR1ParseException>> createTokensSupplier(Supplier<String> stringSupplier) {
        return createSupplier(stringSupplier).map(TokensMapper.instance);
    }

    public Supplier<Result<JSGLRTokens, JSGLR1ParseException>> createTokensSupplier(ResourceKey key) {
        return createTokensSupplier(rsp(key));
    }

    public Supplier<Result<JSGLRTokens, JSGLR1ParseException>> createTokensSupplier(ResourceKey key, ResourceStamper<ReadableResource> stamper) {
        return createTokensSupplier(rsp(key, stamper));
    }

    public Supplier<Result<JSGLRTokens, JSGLR1ParseException>> createTokensSupplier(ResourceKey key, Charset charset) {
        return createTokensSupplier(rsp(key, charset));
    }

    public Supplier<Result<JSGLRTokens, JSGLR1ParseException>> createTokensSupplier(ResourceKey key, ResourceStamper<ReadableResource> stamper, Charset charset) {
        return createTokensSupplier(rsp(key, stamper, charset));
    }


    public Supplier<Messages> createMessagesSupplier(Supplier<String> stringSupplier) {
        return createSupplier(stringSupplier).map(MessagesMapper.instance);
    }

    public Supplier<Messages> createMessagesSupplier(ResourceKey key) {
        return createMessagesSupplier(rsp(key));
    }

    public Supplier<Messages> createMessagesSupplier(ResourceKey key, ResourceStamper<ReadableResource> stamper) {
        return createMessagesSupplier(rsp(key, stamper));
    }

    public Supplier<Messages> createMessagesSupplier(ResourceKey key, Charset charset) {
        return createMessagesSupplier(rsp(key, null, charset));
    }

    public Supplier<Messages> createMessagesSupplier(ResourceKey key, ResourceStamper<ReadableResource> stamper, Charset charset) {
        return createMessagesSupplier(rsp(key, stamper, charset));
    }


    public Function<Supplier<String>, Result<IStrategoTerm, JSGLR1ParseException>> createAstFunction() {
        return createFunction().mapOutput(AstMapper.instance);
    }

    public Function<Supplier<String>, Result<IStrategoTerm, JSGLR1ParseException>> createRecoverableAstFunction() {
        return createFunction().mapOutput(RecoverableAstMapper.instance);
    }

    public Function<Supplier<String>, Result<JSGLRTokens, JSGLR1ParseException>> createTokensFunction() {
        return createFunction().mapOutput(TokensMapper.instance);
    }

    public Function<Supplier<String>, Result<JSGLRTokens, JSGLR1ParseException>> createRecoverableTokensFunction() {
        return createFunction().mapOutput(RecoverableTokensMapper.instance);
    }

    public Function<Supplier<String>, Messages> createMessagesFunction() {
        return createFunction().mapOutput(MessagesMapper.instance);
    }

    // OPTO: create static final instances for each function type.


    private ResourceStringSupplier rsp(ResourceKey key) {
        return new ResourceStringSupplier(key);
    }

    private ResourceStringSupplier rsp(ResourceKey key, ResourceStamper<ReadableResource> stamper) {
        return new ResourceStringSupplier(key, stamper);
    }

    private ResourceStringSupplier rsp(ResourceKey key, Charset charset) {
        return new ResourceStringSupplier(key, null, charset);
    }

    private ResourceStringSupplier rsp(ResourceKey key, ResourceStamper<ReadableResource> stamper, Charset charset) {
        return new ResourceStringSupplier(key, stamper, charset);
    }


    private static @Nullable ResourceKey tryGetResourceKeyFromStringSupplier(Supplier<String> stringSupplier) {
        if(stringSupplier instanceof ResourceStringSupplier) {
            return ((ResourceStringSupplier)stringSupplier).key;
        } else {
            return null;
        }
    }
}


abstract class Mapper<T, R extends Serializable> implements SerializableFunction<T, R> {
    @Override public boolean equals(@Nullable Object other) {
        return this == other || other != null && this.getClass() == other.getClass();
    }

    @Override public int hashCode() { return 0; }

    @Override public String toString() { return getClass().getSimpleName(); }

    // OPTO: deserialize to singleton instance.
}

class AstMapper extends Mapper<Result<JSGLR1ParseOutput, JSGLR1ParseException>, Result<IStrategoTerm, JSGLR1ParseException>> {
    public static final AstMapper instance = new AstMapper();

    @Override
    public Result<IStrategoTerm, JSGLR1ParseException> apply(Result<JSGLR1ParseOutput, JSGLR1ParseException> result) {
        return result.flatMap(r -> {
            if(r.recovered) {
                return Result.ofErr(JSGLR1ParseException.recoveryDisallowedFail(r.messages));
            } else {
                return Result.ofOk(r.ast);
            }
        });
    }

    private AstMapper() {}
}

class RecoverableAstMapper extends Mapper<Result<JSGLR1ParseOutput, JSGLR1ParseException>, Result<IStrategoTerm, JSGLR1ParseException>> {
    public static final RecoverableAstMapper instance = new RecoverableAstMapper();

    @Override
    public Result<IStrategoTerm, JSGLR1ParseException> apply(Result<JSGLR1ParseOutput, JSGLR1ParseException> result) {
        return result.map(r -> r.ast);
    }

    private RecoverableAstMapper() {}
}

class TokensMapper extends Mapper<Result<JSGLR1ParseOutput, JSGLR1ParseException>, Result<JSGLRTokens, JSGLR1ParseException>> {
    public static final TokensMapper instance = new TokensMapper();

    @Override
    public Result<JSGLRTokens, JSGLR1ParseException> apply(Result<JSGLR1ParseOutput, JSGLR1ParseException> result) {
        return result.flatMap(r -> {
            if(r.recovered) {
                return Result.ofErr(JSGLR1ParseException.recoveryDisallowedFail(r.messages));
            } else {
                return Result.ofOk(r.tokens);
            }
        });
    }

    private TokensMapper() {}
}

class RecoverableTokensMapper extends Mapper<Result<JSGLR1ParseOutput, JSGLR1ParseException>, Result<JSGLRTokens, JSGLR1ParseException>> {
    public static final RecoverableTokensMapper instance = new RecoverableTokensMapper();

    @Override
    public Result<JSGLRTokens, JSGLR1ParseException> apply(Result<JSGLR1ParseOutput, JSGLR1ParseException> result) {
        return result.map(r -> r.tokens);
    }

    private RecoverableTokensMapper() {}
}

class MessagesMapper extends Mapper<Result<JSGLR1ParseOutput, JSGLR1ParseException>, Messages> {
    public static final MessagesMapper instance = new MessagesMapper();

    @Override public Messages apply(Result<JSGLR1ParseOutput, JSGLR1ParseException> result) {
        // TODO: output KeyedMessages instead.
        return result.mapOrElse(v -> v.messages.asMessages(), e -> e.getOptionalMessages().map(KeyedMessages::asMessages).orElseGet(Messages::of));
    }

    private MessagesMapper() {}
}
