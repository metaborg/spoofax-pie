package mb.jsglr1.pie;

import mb.common.message.Messages;
import mb.common.result.Result;
import mb.common.util.MapView;
import mb.jsglr.common.JSGLRTokens;
import mb.jsglr1.common.JSGLR1ParseException;
import mb.jsglr1.common.JSGLR1ParseOutput;
import mb.pie.api.ExecContext;
import mb.pie.api.Function;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.Serializable;
import java.io.UncheckedIOException;

public abstract class JSGLR1ParseTaskDef implements TaskDef<JSGLR1ParseTaskInput, Result<JSGLR1ParseOutput, JSGLR1ParseException>> {
    public JSGLR1ParseTaskInput.Builder inputBuilder() {
        return JSGLR1ParseTaskInput.builder(this);
    }


    public Supplier<Result<IStrategoTerm, JSGLR1ParseException>> createAstSupplier(JSGLR1ParseTaskInput input) {
        return createSupplier(input).map(AstFunction.instance);
    }

    public Supplier<Result<IStrategoTerm, JSGLR1ParseException>> createRecoverableAstSupplier(JSGLR1ParseTaskInput input) {
        return createSupplier(input).map(RecoverableAstFunction.instance);
    }

    public Supplier<Result<JSGLRTokens, JSGLR1ParseException>> createTokensSupplier(JSGLR1ParseTaskInput input) {
        return createSupplier(input).map(TokensFunction.instance);
    }

    public Supplier<Result<JSGLRTokens, JSGLR1ParseException>> createRecoverableTokensSupplier(JSGLR1ParseTaskInput input) {
        return createSupplier(input).map(RecoverableTokensFunction.instance);
    }

    public Supplier<Messages> createMessagesSupplier(JSGLR1ParseTaskInput input) {
        return createSupplier(input).map(MessagesFunction.instance);
    }


    public Function<JSGLR1ParseTaskInput, Result<IStrategoTerm, JSGLR1ParseException>> createAstFunction() {
        return astFunction;
    }

    public Function<JSGLR1ParseTaskInput, Result<IStrategoTerm, JSGLR1ParseException>> createRecoverableAstFunction() {
        return recoverableAstFunction;
    }

    public Function<ResourcePath, MapView<ResourceKey, Supplier<Result<IStrategoTerm, JSGLR1ParseException>>>> createMultiAstSupplierFunction(ResourceWalker walker, ResourceMatcher matcher) {
        return new MultiAstSupplierFunction(astFunction, walker, matcher);
    }

    public Function<ResourcePath, MapView<ResourceKey, Supplier<Result<IStrategoTerm, JSGLR1ParseException>>>> createRecoverableMultiAstSupplierFunction(ResourceWalker walker, ResourceMatcher matcher) {
        return new MultiAstSupplierFunction(recoverableAstFunction, walker, matcher);
    }

    public Function<JSGLR1ParseTaskInput, Result<JSGLRTokens, JSGLR1ParseException>> createTokensFunction() {
        return tokensFunction;
    }

    public Function<JSGLR1ParseTaskInput, Result<JSGLRTokens, JSGLR1ParseException>> createRecoverableTokensFunction() {
        return recoverableTokensFunction;
    }

    public Function<JSGLR1ParseTaskInput, Messages> createMessagesFunction() {
        return messagesFunction;
    }

    private final Function<JSGLR1ParseTaskInput, Result<IStrategoTerm, JSGLR1ParseException>> astFunction = createFunction().mapOutput(AstFunction.instance);
    private final Function<JSGLR1ParseTaskInput, Result<IStrategoTerm, JSGLR1ParseException>> recoverableAstFunction = createFunction().mapOutput(RecoverableAstFunction.instance);
    private final Function<JSGLR1ParseTaskInput, Result<JSGLRTokens, JSGLR1ParseException>> tokensFunction = createFunction().mapOutput(TokensFunction.instance);
    private final Function<JSGLR1ParseTaskInput, Result<JSGLRTokens, JSGLR1ParseException>> recoverableTokensFunction = createFunction().mapOutput(RecoverableTokensFunction.instance);
    private final Function<JSGLR1ParseTaskInput, Messages> messagesFunction = createFunction().mapOutput(MessagesFunction.instance);


    protected abstract Result<JSGLR1ParseOutput, JSGLR1ParseException> parse(
        ExecContext context,
        String text,
        @Nullable String startSymbol,
        @Nullable ResourceKey fileHint,
        @Nullable ResourcePath rootDirectoryHint
    ) throws Exception;


    @Override
    public Result<JSGLR1ParseOutput, JSGLR1ParseException> exec(ExecContext context, JSGLR1ParseTaskInput input) throws Exception {
        final @Nullable String startSymbol = input.startSymbol().orElse(null);
        final @Nullable ResourceKey fileHint = input.fileHint().getOr(null);
        final @Nullable ResourcePath rootDirectoryHint = input.rootDirectoryHint().orElse(null);
        try {
            return parse(context, context.require(input.stringSupplier()), startSymbol, fileHint, rootDirectoryHint);
        } catch(UncheckedIOException e) {
            return Result.ofErr(JSGLR1ParseException.readStringFail(e.getCause(), startSymbol, fileHint, rootDirectoryHint));
        }
    }
}
