package mb.jsglr1.pie;

import mb.common.message.Messages;
import mb.common.result.MessagesError;
import mb.common.result.Result;
import mb.common.result.ThrowableError;
import mb.jsglr1.common.JSGLR1ParseOutput;
import mb.jsglr.common.Tokens;
import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
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

public abstract class JSGLR1ParseTaskDef implements TaskDef<Supplier<String>, Result<JSGLR1ParseOutput, MessagesError>> {
    protected abstract Result<JSGLR1ParseOutput, MessagesError> parse(String text) throws InterruptedException;

    @Override
    public Result<JSGLR1ParseOutput, MessagesError> exec(ExecContext context, Supplier<String> stringSupplier) throws InterruptedException {
        try {
            final String text = context.require(stringSupplier);
            return parse(text);
        } catch(ExecException | IOException e) {
            return Result.ofErr(new MessagesError("Parsing failed; cannot get text to parse from '" + stringSupplier + "'", new ThrowableError(e)));
        }
    }


    public Supplier<Result<IStrategoTerm, MessagesError>> createRecoverableAstSupplier(Supplier<String> stringSupplier) {
        return createSupplier(stringSupplier).map(RecoverableAstMapper.instance);
    }

    public Supplier<Result<IStrategoTerm, MessagesError>> createRecoverableAstSupplier(ResourceKey key) {
        return createRecoverableAstSupplier(rsp(key));
    }

    public Supplier<Result<IStrategoTerm, MessagesError>> createRecoverableAstSupplier(ResourceKey key, ResourceStamper<ReadableResource> stamper) {
        return createRecoverableAstSupplier(rsp(key, stamper));
    }

    public Supplier<Result<IStrategoTerm, MessagesError>> createRecoverableAstSupplier(ResourceKey key, Charset charset) {
        return createRecoverableAstSupplier(rsp(key, charset));
    }

    public Supplier<Result<IStrategoTerm, MessagesError>> createRecoverableAstSupplier(ResourceKey key, ResourceStamper<ReadableResource> stamper, Charset charset) {
        return createRecoverableAstSupplier(rsp(key, stamper, charset));
    }


    public Supplier<Result<IStrategoTerm, MessagesError>> createAstSupplier(Supplier<String> stringSupplier) {
        return createSupplier(stringSupplier).map(AstMapper.instance);
    }

    public Supplier<Result<IStrategoTerm, MessagesError>> createAstSupplier(ResourceKey key) {
        return createAstSupplier(rsp(key));
    }

    public Supplier<Result<IStrategoTerm, MessagesError>> createAstSupplier(ResourceKey key, ResourceStamper<ReadableResource> stamper) {
        return createAstSupplier(rsp(key, stamper));
    }

    public Supplier<Result<IStrategoTerm, MessagesError>> createAstSupplier(ResourceKey key, Charset charset) {
        return createAstSupplier(rsp(key, charset));
    }

    public Supplier<Result<IStrategoTerm, MessagesError>> createAstSupplier(ResourceKey key, ResourceStamper<ReadableResource> stamper, Charset charset) {
        return createAstSupplier(rsp(key, stamper, charset));
    }


    public Supplier<Result<Tokens, MessagesError>> createRecoverableTokensSupplier(Supplier<String> stringSupplier) {
        return createSupplier(stringSupplier).map(RecoverableTokensMapper.instance);
    }

    public Supplier<Result<Tokens, MessagesError>> createRecoverableTokensSupplier(ResourceKey key) {
        return createRecoverableTokensSupplier(rsp(key));
    }

    public Supplier<Result<Tokens, MessagesError>> createRecoverableTokensSupplier(ResourceKey key, ResourceStamper<ReadableResource> stamper) {
        return createRecoverableTokensSupplier(rsp(key, stamper));
    }

    public Supplier<Result<Tokens, MessagesError>> createRecoverableTokensSupplier(ResourceKey key, Charset charset) {
        return createRecoverableTokensSupplier(rsp(key, charset));
    }

    public Supplier<Result<Tokens, MessagesError>> createRecoverableTokensSupplier(ResourceKey key, ResourceStamper<ReadableResource> stamper, Charset charset) {
        return createRecoverableTokensSupplier(rsp(key, stamper, charset));
    }


    public Supplier<Result<Tokens, MessagesError>> createTokensSupplier(Supplier<String> stringSupplier) {
        return createSupplier(stringSupplier).map(TokensMapper.instance);
    }

    public Supplier<Result<Tokens, MessagesError>> createTokensSupplier(ResourceKey key) {
        return createTokensSupplier(rsp(key));
    }

    public Supplier<Result<Tokens, MessagesError>> createTokensSupplier(ResourceKey key, ResourceStamper<ReadableResource> stamper) {
        return createTokensSupplier(rsp(key, stamper));
    }

    public Supplier<Result<Tokens, MessagesError>> createTokensSupplier(ResourceKey key, Charset charset) {
        return createTokensSupplier(rsp(key, charset));
    }

    public Supplier<Result<Tokens, MessagesError>> createTokensSupplier(ResourceKey key, ResourceStamper<ReadableResource> stamper, Charset charset) {
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


    public Function<Supplier<String>, Result<IStrategoTerm, MessagesError>> createAstFunction() {
        return createFunction().mapOutput(AstMapper.instance);
    }

    public Function<Supplier<String>, Result<IStrategoTerm, MessagesError>> createRecoverableAstFunction() {
        return createFunction().mapOutput(RecoverableAstMapper.instance);
    }

    public Function<Supplier<String>, Result<Tokens, MessagesError>> createTokensFunction() {
        return createFunction().mapOutput(TokensMapper.instance);
    }

    public Function<Supplier<String>, Result<Tokens, MessagesError>> createRecoverableTokensFunction() {
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

class AstMapper extends Mapper<Result<JSGLR1ParseOutput, MessagesError>, Result<IStrategoTerm, MessagesError>> {
    public static final AstMapper instance = new AstMapper();

    @Override public Result<IStrategoTerm, MessagesError> apply(Result<JSGLR1ParseOutput, MessagesError> result) {
        return result.andThen(r -> {
            if(r.recovered) {
                return Result.ofErr(new MessagesError("Parser produced a recovered AST, but recovery was disallowed", r.messages));
            } else {
                return Result.ofOk(r.ast);
            }
        });
    }

    private AstMapper() {}
}

class RecoverableAstMapper extends Mapper<Result<JSGLR1ParseOutput, MessagesError>, Result<IStrategoTerm, MessagesError>> {
    public static final RecoverableAstMapper instance = new RecoverableAstMapper();

    @Override public Result<IStrategoTerm, MessagesError> apply(Result<JSGLR1ParseOutput, MessagesError> result) {
        return result.map(r -> r.ast);
    }

    private RecoverableAstMapper() {}
}

class TokensMapper extends Mapper<Result<JSGLR1ParseOutput, MessagesError>, Result<Tokens, MessagesError>> {
    public static final TokensMapper instance = new TokensMapper();

    @Override public Result<Tokens, MessagesError> apply(Result<JSGLR1ParseOutput, MessagesError> result) {
        return result.andThen(r -> {
            if(r.recovered) {
                return Result.ofErr(new MessagesError("Parser produced recovered tokens, but recovery was disallowed", r.messages));
            } else {
                return Result.ofOk(r.tokens);
            }
        });
    }

    private TokensMapper() {}
}

class RecoverableTokensMapper extends Mapper<Result<JSGLR1ParseOutput, MessagesError>, Result<Tokens, MessagesError>> {
    public static final RecoverableTokensMapper instance = new RecoverableTokensMapper();

    @Override public Result<Tokens, MessagesError> apply(Result<JSGLR1ParseOutput, MessagesError> result) {
        return result.map(r -> r.tokens);
    }

    private RecoverableTokensMapper() {}
}

class MessagesMapper extends Mapper<Result<JSGLR1ParseOutput, MessagesError>, Messages> {
    public static final MessagesMapper instance = new MessagesMapper();

    @Override public Messages apply(Result<JSGLR1ParseOutput, MessagesError> result) {
        return result.mapRes(v -> v.messages, MessagesError::getMessages);
    }

    private MessagesMapper() {}
}
