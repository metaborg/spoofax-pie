package mb.jsglr1.pie;

import mb.common.message.Messages;
import mb.common.result.MessagesException;
import mb.common.result.Result;
import mb.jsglr.common.JSGLRTokens;
import mb.jsglr1.common.JSGLR1ParseOutput;
import mb.pie.api.ExecContext;
import mb.pie.api.Function;
import mb.pie.api.ResourceStringSupplier;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.ResourceStamper;
import mb.resource.ReadableResource;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;

public abstract class JSGLR1ParseTaskDef implements TaskDef<Supplier<String>, Result<JSGLR1ParseOutput, MessagesException>> {
    protected abstract Result<JSGLR1ParseOutput, MessagesException> parse(String text) throws InterruptedException;

    @Override
    public Result<JSGLR1ParseOutput, MessagesException> exec(ExecContext context, Supplier<String> stringSupplier) throws InterruptedException {
        try {
            return parse(context.require(stringSupplier));
        } catch(IOException e) {
            return Result.ofErr(new MessagesException("Parsing failed; cannot get text to parse from '" + stringSupplier + "'", e));
        }
    }


    public Supplier<Result<IStrategoTerm, MessagesException>> createRecoverableAstSupplier(Supplier<String> stringSupplier) {
        return createSupplier(stringSupplier).map(RecoverableAstMapper.instance);
    }

    public Supplier<Result<IStrategoTerm, MessagesException>> createRecoverableAstSupplier(ResourceKey key) {
        return createRecoverableAstSupplier(rsp(key));
    }

    public Supplier<Result<IStrategoTerm, MessagesException>> createRecoverableAstSupplier(ResourceKey key, ResourceStamper<ReadableResource> stamper) {
        return createRecoverableAstSupplier(rsp(key, stamper));
    }

    public Supplier<Result<IStrategoTerm, MessagesException>> createRecoverableAstSupplier(ResourceKey key, Charset charset) {
        return createRecoverableAstSupplier(rsp(key, charset));
    }

    public Supplier<Result<IStrategoTerm, MessagesException>> createRecoverableAstSupplier(ResourceKey key, ResourceStamper<ReadableResource> stamper, Charset charset) {
        return createRecoverableAstSupplier(rsp(key, stamper, charset));
    }


    public Supplier<Result<IStrategoTerm, MessagesException>> createAstSupplier(Supplier<String> stringSupplier) {
        return createSupplier(stringSupplier).map(AstMapper.instance); // TODO: pass an outputstamper that only does an equals check on the AST.
    }

    public Supplier<Result<IStrategoTerm, MessagesException>> createAstSupplier(ResourceKey key) {
        return createAstSupplier(rsp(key));
    }

    public Supplier<Result<IStrategoTerm, MessagesException>> createAstSupplier(ResourceKey key, ResourceStamper<ReadableResource> stamper) {
        return createAstSupplier(rsp(key, stamper));
    }

    public Supplier<Result<IStrategoTerm, MessagesException>> createAstSupplier(ResourceKey key, Charset charset) {
        return createAstSupplier(rsp(key, charset));
    }

    public Supplier<Result<IStrategoTerm, MessagesException>> createAstSupplier(ResourceKey key, ResourceStamper<ReadableResource> stamper, Charset charset) {
        return createAstSupplier(rsp(key, stamper, charset));
    }


    public Supplier<Result<JSGLRTokens, MessagesException>> createRecoverableTokensSupplier(Supplier<String> stringSupplier) {
        return createSupplier(stringSupplier).map(RecoverableTokensMapper.instance);
    }

    public Supplier<Result<JSGLRTokens, MessagesException>> createRecoverableTokensSupplier(ResourceKey key) {
        return createRecoverableTokensSupplier(rsp(key));
    }

    public Supplier<Result<JSGLRTokens, MessagesException>> createRecoverableTokensSupplier(ResourceKey key, ResourceStamper<ReadableResource> stamper) {
        return createRecoverableTokensSupplier(rsp(key, stamper));
    }

    public Supplier<Result<JSGLRTokens, MessagesException>> createRecoverableTokensSupplier(ResourceKey key, Charset charset) {
        return createRecoverableTokensSupplier(rsp(key, charset));
    }

    public Supplier<Result<JSGLRTokens, MessagesException>> createRecoverableTokensSupplier(ResourceKey key, ResourceStamper<ReadableResource> stamper, Charset charset) {
        return createRecoverableTokensSupplier(rsp(key, stamper, charset));
    }


    public Supplier<Result<JSGLRTokens, MessagesException>> createTokensSupplier(Supplier<String> stringSupplier) {
        return createSupplier(stringSupplier).map(TokensMapper.instance);
    }

    public Supplier<Result<JSGLRTokens, MessagesException>> createTokensSupplier(ResourceKey key) {
        return createTokensSupplier(rsp(key));
    }

    public Supplier<Result<JSGLRTokens, MessagesException>> createTokensSupplier(ResourceKey key, ResourceStamper<ReadableResource> stamper) {
        return createTokensSupplier(rsp(key, stamper));
    }

    public Supplier<Result<JSGLRTokens, MessagesException>> createTokensSupplier(ResourceKey key, Charset charset) {
        return createTokensSupplier(rsp(key, charset));
    }

    public Supplier<Result<JSGLRTokens, MessagesException>> createTokensSupplier(ResourceKey key, ResourceStamper<ReadableResource> stamper, Charset charset) {
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


    public Function<Supplier<String>, Result<IStrategoTerm, MessagesException>> createAstFunction() {
        return createFunction().mapOutput(AstMapper.instance);
    }

    public Function<Supplier<String>, Result<IStrategoTerm, MessagesException>> createRecoverableAstFunction() {
        return createFunction().mapOutput(RecoverableAstMapper.instance);
    }

    public Function<Supplier<String>, Result<JSGLRTokens, MessagesException>> createTokensFunction() {
        return createFunction().mapOutput(TokensMapper.instance);
    }

    public Function<Supplier<String>, Result<JSGLRTokens, MessagesException>> createRecoverableTokensFunction() {
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
}


abstract class Mapper<T, R> implements java.util.function.Function<T, R>, Serializable {
    @Override public boolean equals(@Nullable Object other) {
        return this == other || other != null && this.getClass() == other.getClass();
    }

    @Override public int hashCode() { return 0; }

    @Override public String toString() { return getClass().getSimpleName(); }

    // OPTO: deserialize to singleton instance.
}

class AstMapper extends Mapper<Result<JSGLR1ParseOutput, MessagesException>, Result<IStrategoTerm, MessagesException>> {
    public static final AstMapper instance = new AstMapper();

    @Override public Result<IStrategoTerm, MessagesException> apply(Result<JSGLR1ParseOutput, MessagesException> result) {
        return result.flatMap(r -> {
            if(r.recovered) {
                return Result.ofErr(new MessagesException(r.messages, "Parser produced a recovered AST, but recovery was disallowed"));
            } else {
                return Result.ofOk(r.ast);
            }
        });
    }

    private AstMapper() {}
}

class RecoverableAstMapper extends Mapper<Result<JSGLR1ParseOutput, MessagesException>, Result<IStrategoTerm, MessagesException>> {
    public static final RecoverableAstMapper instance = new RecoverableAstMapper();

    @Override public Result<IStrategoTerm, MessagesException> apply(Result<JSGLR1ParseOutput, MessagesException> result) {
        return result.map(r -> r.ast);
    }

    private RecoverableAstMapper() {}
}

class TokensMapper extends Mapper<Result<JSGLR1ParseOutput, MessagesException>, Result<JSGLRTokens, MessagesException>> {
    public static final TokensMapper instance = new TokensMapper();

    @Override public Result<JSGLRTokens, MessagesException> apply(Result<JSGLR1ParseOutput, MessagesException> result) {
        return result.flatMap(r -> {
            if(r.recovered) {
                return Result.ofErr(new MessagesException(r.messages, "Parser produced recovered tokens, but recovery was disallowed"));
            } else {
                return Result.ofOk(r.tokens);
            }
        });
    }

    private TokensMapper() {}
}

class RecoverableTokensMapper extends Mapper<Result<JSGLR1ParseOutput, MessagesException>, Result<JSGLRTokens, MessagesException>> {
    public static final RecoverableTokensMapper instance = new RecoverableTokensMapper();

    @Override public Result<JSGLRTokens, MessagesException> apply(Result<JSGLR1ParseOutput, MessagesException> result) {
        return result.map(r -> r.tokens);
    }

    private RecoverableTokensMapper() {}
}

class MessagesMapper extends Mapper<Result<JSGLR1ParseOutput, MessagesException>, Messages> {
    public static final MessagesMapper instance = new MessagesMapper();

    @Override public Messages apply(Result<JSGLR1ParseOutput, MessagesException> result) {
        return result.mapOrElse(v -> v.messages, MessagesException::getMessages);
    }

    private MessagesMapper() {}
}
