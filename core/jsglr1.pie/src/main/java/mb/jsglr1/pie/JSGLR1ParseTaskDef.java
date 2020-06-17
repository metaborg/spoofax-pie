package mb.jsglr1.pie;

import mb.common.message.Message;
import mb.common.message.Messages;
import mb.common.token.Token;
import mb.jsglr1.common.JSGLR1ParseResult;
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
import java.util.ArrayList;

public abstract class JSGLR1ParseTaskDef implements TaskDef<Supplier<String>, JSGLR1ParseResult> {
    protected abstract JSGLR1ParseResult parse(String text) throws InterruptedException;

    @Override
    public JSGLR1ParseResult exec(ExecContext context, Supplier<String> stringSupplier) throws InterruptedException {
        final String text;
        try {
            text = context.require(stringSupplier);
        } catch(IOException e) {
            return JSGLR1ParseResult.failed(Messages.of(new Message("Cannot get text to parse from '" + stringSupplier + "'", e)));
        }
        return parse(text);
    }


    // AST supplier that throws on parse failures, including recovered ASTs.

    public Supplier<IStrategoTerm> createAstSupplier(Supplier<String> stringSupplier) {
        return createSupplier(stringSupplier).map(ThrowsAstMapper.instance);
    }

    public Supplier<IStrategoTerm> createAstSupplier(ResourceKey key) {
        return createAstSupplier(rsp(key));
    }

    public Supplier<IStrategoTerm> createAstSupplier(ResourceKey key, ResourceStamper<ReadableResource> stamper) {
        return createAstSupplier(rsp(key, stamper));
    }

    public Supplier<IStrategoTerm> createAstSupplier(ResourceKey key, Charset charset) {
        return createAstSupplier(rsp(key, charset));
    }

    public Supplier<IStrategoTerm> createAstSupplier(ResourceKey key, ResourceStamper<ReadableResource> stamper, Charset charset) {
        return createAstSupplier(rsp(key, stamper, charset));
    }

    // AST supplier that throws on parse failures, excluding recovered ASTs.

    public Supplier<IStrategoTerm> createRecoverableAstSupplier(Supplier<String> stringSupplier) {
        return createSupplier(stringSupplier).map(ThrowsRecoverableAstMapper.instance);
    }

    public Supplier<IStrategoTerm> createRecoverableAstSupplier(ResourceKey key) {
        return createRecoverableAstSupplier(rsp(key));
    }

    public Supplier<IStrategoTerm> createRecoverableAstSupplier(ResourceKey key, ResourceStamper<ReadableResource> stamper) {
        return createRecoverableAstSupplier(rsp(key, stamper));
    }

    public Supplier<IStrategoTerm> createRecoverableAstSupplier(ResourceKey key, Charset charset) {
        return createRecoverableAstSupplier(rsp(key, charset));
    }

    public Supplier<IStrategoTerm> createRecoverableAstSupplier(ResourceKey key, ResourceStamper<ReadableResource> stamper, Charset charset) {
        return createRecoverableAstSupplier(rsp(key, stamper, charset));
    }

    // AST supplier that returns null on parse failures, including recovered ASTs.

    public Supplier<IStrategoTerm> createNullableAstSupplier(Supplier<String> stringSupplier) {
        return createSupplier(stringSupplier).map(NullableAstMapper.instance);
    }

    public Supplier<IStrategoTerm> createNullableAstSupplier(ResourceKey key) {
        return createNullableAstSupplier(rsp(key));
    }

    public Supplier<IStrategoTerm> createNullableAstSupplier(ResourceKey key, ResourceStamper<ReadableResource> stamper) {
        return createNullableAstSupplier(rsp(key, stamper));
    }

    public Supplier<IStrategoTerm> createNullableAstSupplier(ResourceKey key, Charset charset) {
        return createNullableAstSupplier(rsp(key, charset));
    }

    public Supplier<IStrategoTerm> createNullableAstSupplier(ResourceKey key, ResourceStamper<ReadableResource> stamper, Charset charset) {
        return createNullableAstSupplier(rsp(key, stamper, charset));
    }

    // AST supplier that returns null on parse failures, excluding recovered ASTs.

    public Supplier<IStrategoTerm> createNullableRecoverableAstSupplier(Supplier<String> stringSupplier) {
        return createSupplier(stringSupplier).map(NullableRecoverableAstMapper.instance);
    }

    public Supplier<IStrategoTerm> createNullableRecoverableAstSupplier(ResourceKey key) {
        return createNullableRecoverableAstSupplier(rsp(key));
    }

    public Supplier<IStrategoTerm> createNullableRecoverableAstSupplier(ResourceKey key, ResourceStamper<ReadableResource> stamper) {
        return createNullableRecoverableAstSupplier(rsp(key, stamper));
    }

    public Supplier<IStrategoTerm> createNullableRecoverableAstSupplier(ResourceKey key, Charset charset) {
        return createNullableRecoverableAstSupplier(rsp(key, charset));
    }

    public Supplier<IStrategoTerm> createNullableRecoverableAstSupplier(ResourceKey key, ResourceStamper<ReadableResource> stamper, Charset charset) {
        return createNullableRecoverableAstSupplier(rsp(key, stamper, charset));
    }


    // Tokens supplier that throws on parse failures, including recovered tokens.

    public Supplier<ArrayList<? extends Token<IStrategoTerm>>> createTokensSupplier(Supplier<String> stringSupplier) {
        return createSupplier(stringSupplier).map(ThrowsTokensMapper.instance);
    }

    public Supplier<ArrayList<? extends Token<IStrategoTerm>>> createTokensSupplier(ResourceKey key) {
        return createTokensSupplier(rsp(key));
    }

    public Supplier<ArrayList<? extends Token<IStrategoTerm>>> createTokensSupplier(ResourceKey key, ResourceStamper<ReadableResource> stamper) {
        return createTokensSupplier(rsp(key, stamper));
    }

    public Supplier<ArrayList<? extends Token<IStrategoTerm>>> createTokensSupplier(ResourceKey key, Charset charset) {
        return createTokensSupplier(rsp(key, charset));
    }

    public Supplier<ArrayList<? extends Token<IStrategoTerm>>> createTokensSupplier(ResourceKey key, ResourceStamper<ReadableResource> stamper, Charset charset) {
        return createTokensSupplier(rsp(key, stamper, charset));
    }

    // Tokens supplier that throws on parse failures, excluding recovered tokens.

    public Supplier<ArrayList<? extends Token<IStrategoTerm>>> createRecoverableTokensSupplier(Supplier<String> stringSupplier) {
        return createSupplier(stringSupplier).map(ThrowsRecoverableTokensMapper.instance);
    }

    public Supplier<ArrayList<? extends Token<IStrategoTerm>>> createRecoverableTokensSupplier(ResourceKey key) {
        return createRecoverableTokensSupplier(rsp(key));
    }

    public Supplier<ArrayList<? extends Token<IStrategoTerm>>> createRecoverableTokensSupplier(ResourceKey key, ResourceStamper<ReadableResource> stamper) {
        return createRecoverableTokensSupplier(rsp(key, stamper));
    }

    public Supplier<ArrayList<? extends Token<IStrategoTerm>>> createRecoverableTokensSupplier(ResourceKey key, Charset charset) {
        return createRecoverableTokensSupplier(rsp(key, charset));
    }

    public Supplier<ArrayList<? extends Token<IStrategoTerm>>> createRecoverableTokensSupplier(ResourceKey key, ResourceStamper<ReadableResource> stamper, Charset charset) {
        return createRecoverableTokensSupplier(rsp(key, stamper, charset));
    }

    // Tokens supplier that returns null on parse failures, including recovered tokens.

    public Supplier<@Nullable ArrayList<? extends Token<IStrategoTerm>>> createNullableTokensSupplier(Supplier<String> stringSupplier) {
        return createSupplier(stringSupplier).map(NullableTokensMapper.instance);
    }

    public Supplier<@Nullable ArrayList<? extends Token<IStrategoTerm>>> createNullableTokensSupplier(ResourceKey key) {
        return createNullableTokensSupplier(rsp(key));
    }

    public Supplier<@Nullable ArrayList<? extends Token<IStrategoTerm>>> createNullableTokensSupplier(ResourceKey key, ResourceStamper<ReadableResource> stamper) {
        return createNullableTokensSupplier(rsp(key, stamper));
    }

    public Supplier<@Nullable ArrayList<? extends Token<IStrategoTerm>>> createNullableTokensSupplier(ResourceKey key, Charset charset) {
        return createNullableTokensSupplier(rsp(key, charset));
    }

    public Supplier<@Nullable ArrayList<? extends Token<IStrategoTerm>>> createNullableTokensSupplier(ResourceKey key, ResourceStamper<ReadableResource> stamper, Charset charset) {
        return createNullableTokensSupplier(rsp(key, stamper, charset));
    }

    // Tokens supplier that returns null on parse failures, excluding recovered tokens.

    public Supplier<@Nullable ArrayList<? extends Token<IStrategoTerm>>> createNullableRecoverableTokensSupplier(Supplier<String> stringSupplier) {
        return createSupplier(stringSupplier).map(NullableRecoverableTokensMapper.instance);
    }

    public Supplier<@Nullable ArrayList<? extends Token<IStrategoTerm>>> createNullableRecoverableTokensSupplier(ResourceKey key) {
        return createNullableRecoverableTokensSupplier(rsp(key));
    }

    public Supplier<@Nullable ArrayList<? extends Token<IStrategoTerm>>> createNullableRecoverableTokensSupplier(ResourceKey key, ResourceStamper<ReadableResource> stamper) {
        return createNullableRecoverableTokensSupplier(rsp(key, stamper));
    }

    public Supplier<@Nullable ArrayList<? extends Token<IStrategoTerm>>> createNullableRecoverableTokensSupplier(ResourceKey key, Charset charset) {
        return createNullableRecoverableTokensSupplier(rsp(key, charset));
    }

    public Supplier<@Nullable ArrayList<? extends Token<IStrategoTerm>>> createNullableRecoverableTokensSupplier(ResourceKey key, ResourceStamper<ReadableResource> stamper, Charset charset) {
        return createNullableRecoverableTokensSupplier(rsp(key, stamper, charset));
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


    public Function<Supplier<String>, IStrategoTerm> createAstFunction() {
        return createFunction().mapOutput(ThrowsAstMapper.instance);
    }

    public Function<Supplier<String>, IStrategoTerm> createRecoverableAstFunction() {
        return createFunction().mapOutput(ThrowsRecoverableAstMapper.instance);
    }

    public Function<Supplier<String>, @Nullable IStrategoTerm> createNullableAstFunction() {
        return createFunction().mapOutput(NullableAstMapper.instance);
    }

    public Function<Supplier<String>, @Nullable IStrategoTerm> createNullableRecoverableAstFunction() {
        return createFunction().mapOutput(NullableRecoverableAstMapper.instance);
    }


    public Function<Supplier<String>, ArrayList<? extends Token<IStrategoTerm>>> createTokensFunction() {
        return createFunction().mapOutput(ThrowsTokensMapper.instance);
    }

    public Function<Supplier<String>, ArrayList<? extends Token<IStrategoTerm>>> createRecoverableTokensFunction() {
        return createFunction().mapOutput(ThrowsRecoverableTokensMapper.instance);
    }

    public Function<Supplier<String>, @Nullable ArrayList<? extends Token<IStrategoTerm>>> createNullableTokensFunction() {
        return createFunction().mapOutput(NullableTokensMapper.instance);
    }

    public Function<Supplier<String>, @Nullable ArrayList<? extends Token<IStrategoTerm>>> createNullableRecoverableTokensFunction() {
        return createFunction().mapOutput(NullableRecoverableTokensMapper.instance);
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


class ThrowsAstMapper extends Mapper<JSGLR1ParseResult, IStrategoTerm> {
    public static final ThrowsAstMapper instance = new ThrowsAstMapper();

    @Override public @Nullable IStrategoTerm apply(JSGLR1ParseResult result) {
        return result.caseOf()
            .success((ast, tokens, messages) -> ast)
            .otherwiseEmpty().orElseThrow(() -> new ParseFailedException(result.getMessages()));
    }

    private ThrowsAstMapper() {}
}

class ThrowsRecoverableAstMapper extends Mapper<JSGLR1ParseResult, IStrategoTerm> {
    public static final ThrowsRecoverableAstMapper instance = new ThrowsRecoverableAstMapper();

    @Override public @Nullable IStrategoTerm apply(JSGLR1ParseResult result) {
        return result
            .getAst()
            .orElseThrow(() -> new ParseFailedException(result.getMessages()));
    }

    private ThrowsRecoverableAstMapper() {}
}

class NullableAstMapper extends Mapper<JSGLR1ParseResult, @Nullable IStrategoTerm> {
    public static final NullableAstMapper instance = new NullableAstMapper();

    @SuppressWarnings("ConstantConditions") @Override public @Nullable IStrategoTerm apply(JSGLR1ParseResult result) {
        return result.caseOf()
            .success((ast, tokens, messages) -> ast)
            .otherwise_(null);
    }

    private NullableAstMapper() {}
}

class NullableRecoverableAstMapper extends Mapper<JSGLR1ParseResult, @Nullable IStrategoTerm> {
    public static final NullableRecoverableAstMapper instance = new NullableRecoverableAstMapper();

    @Override public @Nullable IStrategoTerm apply(JSGLR1ParseResult result) {
        return result
            .getAst()
            .orElse(null);
    }

    private NullableRecoverableAstMapper() {}
}


class ThrowsTokensMapper extends Mapper<JSGLR1ParseResult, ArrayList<? extends Token<IStrategoTerm>>> {
    public static final ThrowsTokensMapper instance = new ThrowsTokensMapper();

    @Override public @Nullable ArrayList<? extends Token<IStrategoTerm>> apply(JSGLR1ParseResult result) {
        return result.caseOf()
            .success((ast, tokens, messages) -> tokens)
            .otherwiseEmpty().orElseThrow(() -> new ParseFailedException(result.getMessages()));
    }

    private ThrowsTokensMapper() {}
}

class ThrowsRecoverableTokensMapper extends Mapper<JSGLR1ParseResult, ArrayList<? extends Token<IStrategoTerm>>> {
    public static final ThrowsRecoverableTokensMapper instance = new ThrowsRecoverableTokensMapper();

    @Override public @Nullable ArrayList<? extends Token<IStrategoTerm>> apply(JSGLR1ParseResult result) {
        return result
            .getTokens()
            .orElseThrow(() -> new ParseFailedException(result.getMessages()));
    }

    private ThrowsRecoverableTokensMapper() {}
}

class NullableTokensMapper extends Mapper<JSGLR1ParseResult, @Nullable ArrayList<? extends Token<IStrategoTerm>>> {
    public static final NullableTokensMapper instance = new NullableTokensMapper();

    @SuppressWarnings("ConstantConditions") @Override
    public @Nullable ArrayList<? extends Token<IStrategoTerm>> apply(JSGLR1ParseResult result) {
        return result.caseOf()
            .success((ast, tokens, messages) -> tokens)
            .otherwise_(null);
    }

    private NullableTokensMapper() {}
}

class NullableRecoverableTokensMapper extends Mapper<JSGLR1ParseResult, @Nullable ArrayList<? extends Token<IStrategoTerm>>> {
    public static final NullableRecoverableTokensMapper instance = new NullableRecoverableTokensMapper();

    @Override public @Nullable ArrayList<? extends Token<IStrategoTerm>> apply(JSGLR1ParseResult result) {
        return result
            .getTokens()
            .orElse(null);
    }

    private NullableRecoverableTokensMapper() {}
}


class MessagesMapper extends Mapper<JSGLR1ParseResult, Messages> {
    public static final MessagesMapper instance = new MessagesMapper();

    @Override public Messages apply(JSGLR1ParseResult result) {
        return result.getMessages();
    }

    private MessagesMapper() {}
}
