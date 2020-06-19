package mb.tiger.spoofax.task.reusable;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.common.StrategoUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.Serializable;

public class TigerListLiteralVals implements TaskDef<Supplier<? extends Result<IStrategoTerm, ?>>, Result<String, ? super Exception>> {
    private final Provider<StrategoRuntime> strategoRuntimeProvider;

    @Inject
    public TigerListLiteralVals(Provider<StrategoRuntime> strategoRuntimeProvider) {
        this.strategoRuntimeProvider = strategoRuntimeProvider;
    }

    @Override
    public String getId() { return "mb.tiger.spoofax.task.reusable.TigerListLiteralVals"; }

    @Override
    public Result<String, ? super Exception> exec(ExecContext context, Supplier<? extends Result<IStrategoTerm, ?>> astSupplier) throws Exception {
        return context.require(astSupplier)
            .flatMapOrElse((ast) -> {
                final StrategoRuntime strategoRuntime = strategoRuntimeProvider.get();
                final String strategyId = "list-of-literal-vals";
                // TODO: strategoRuntime.invoke should return a Result that handles what the following code does.
                // TODO: strategoRuntime.invoke should additionally support a term format to report messages, which can
                //       then be returned as a (Keyed)MessagesError.
                try {
                    final @Nullable IStrategoTerm result = strategoRuntime.invoke(strategyId, ast);
                    return Result.ofNullableOrElse(result, () -> new Exception("Invoking '" + strategyId + "' on '" + ast + "' failed unexpectedly"));
                } catch(StrategoException e) {
                    return Result.ofErr(e);
                }
            }, Result::ofErr)
            .map((ast) -> StrategoUtil.toString(ast, Integer.MAX_VALUE));
    }
}
