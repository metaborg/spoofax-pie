package mb.jsglr.pie;

import mb.common.message.Messages;
import mb.common.result.Result;
import mb.common.text.Text;
import mb.common.util.ListView;
import mb.jsglr.common.JSGLRTokens;
import mb.jsglr.common.JsglrParseException;
import mb.jsglr.common.JsglrParseOutput;
import mb.pie.api.ExecContext;
import mb.pie.api.Function;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.IOException;
import java.io.UncheckedIOException;

public abstract class JsglrParseTaskDef implements TaskDef<JsglrParseTaskInput, Result<JsglrParseOutput, JsglrParseException>> {
    public JsglrParseTaskInput.Builder inputBuilder() {
        return JsglrParseTaskInput.builder(this);
    }


    public Supplier<Result<IStrategoTerm, JsglrParseException>> createAstSupplier(JsglrParseTaskInput input) {
        return createSupplier(input).map(AstFunction.instance);
    }

    public Supplier<Result<IStrategoTerm, JsglrParseException>> createRecoverableAstSupplier(JsglrParseTaskInput input) {
        return createSupplier(input).map(RecoverableAstFunction.instance);
    }

    public Supplier<Result<JSGLRTokens, JsglrParseException>> createTokensSupplier(JsglrParseTaskInput input) {
        return createSupplier(input).map(TokensFunction.instance);
    }

    public Supplier<Result<JSGLRTokens, JsglrParseException>> createRecoverableTokensSupplier(JsglrParseTaskInput input) {
        return createSupplier(input).map(RecoverableTokensFunction.instance);
    }

    public Supplier<Messages> createMessagesSupplier(JsglrParseTaskInput input) {
        return createSupplier(input).map(MessagesFunction.instance);
    }


    public Function<JsglrParseTaskInput, Result<IStrategoTerm, JsglrParseException>> createAstFunction() {
        return astFunction;
    }

    public Function<JsglrParseTaskInput, Result<IStrategoTerm, JsglrParseException>> createRecoverableAstFunction() {
        return recoverableAstFunction;
    }


    public MultiAstSupplierFunction createMultiAstSupplierFunction(Function<ResourcePath, ? extends ListView<? extends ResourceKey>> sourceFilesFunction) {
        return new MultiAstSupplierFunction(sourceFilesFunction, astFunction);
    }

    public MultiAstSupplierFunction createRecoverableMultiAstSupplierFunction(Function<ResourcePath, ? extends ListView<? extends ResourceKey>> sourceFilesFunction) {
        return new MultiAstSupplierFunction(sourceFilesFunction, recoverableAstFunction);
    }


    public Function<JsglrParseTaskInput, Result<JSGLRTokens, JsglrParseException>> createTokensFunction() {
        return tokensFunction;
    }

    public Function<JsglrParseTaskInput, Result<JSGLRTokens, JsglrParseException>> createRecoverableTokensFunction() {
        return recoverableTokensFunction;
    }

    public Function<JsglrParseTaskInput, Messages> createMessagesFunction() {
        return messagesFunction;
    }


    private final Function<JsglrParseTaskInput, Result<IStrategoTerm, JsglrParseException>> astFunction = createFunction().mapOutput(AstFunction.instance);
    private final Function<JsglrParseTaskInput, Result<IStrategoTerm, JsglrParseException>> recoverableAstFunction = createFunction().mapOutput(RecoverableAstFunction.instance);
    private final Function<JsglrParseTaskInput, Result<JSGLRTokens, JsglrParseException>> tokensFunction = createFunction().mapOutput(TokensFunction.instance);
    private final Function<JsglrParseTaskInput, Result<JSGLRTokens, JsglrParseException>> recoverableTokensFunction = createFunction().mapOutput(RecoverableTokensFunction.instance);
    private final Function<JsglrParseTaskInput, Messages> messagesFunction = createFunction().mapOutput(MessagesFunction.instance);


    protected abstract Result<JsglrParseOutput, JsglrParseException> parse(
        ExecContext context,
        Text text,
        @Nullable String startSymbol,
        @Nullable ResourceKey fileHint,
        @Nullable ResourcePath rootDirectoryHint
    ) throws IOException, InterruptedException;


    @Override
    public Result<JsglrParseOutput, JsglrParseException> exec(ExecContext context, JsglrParseTaskInput input) throws Exception {
        final @Nullable String startSymbol = input.startSymbol().orElse(null);
        final @Nullable ResourceKey fileHint = input.fileHint().getOr(null);
        final @Nullable ResourcePath rootDirectoryHint = input.rootDirectoryHint().orElse(null);
        try {
            return parse(context, context.require(input.textSupplier()), startSymbol, fileHint, rootDirectoryHint);
        } catch(UncheckedIOException e) {
            return Result.ofErr(JsglrParseException.readStringFail(e.getCause(), startSymbol, fileHint, rootDirectoryHint));
        }
    }
}
