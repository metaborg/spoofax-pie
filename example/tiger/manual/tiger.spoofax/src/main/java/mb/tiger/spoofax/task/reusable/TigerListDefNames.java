package mb.tiger.spoofax.task.reusable;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.common.StrategoUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import javax.inject.Provider;

public class TigerListDefNames implements TaskDef<Supplier<? extends Result<IStrategoTerm, ?>>, Result<String, ?>> {
    private final Provider<StrategoRuntime> strategoRuntimeProvider;

    @Inject
    public TigerListDefNames(Provider<StrategoRuntime> strategoRuntimeProvider) {
        this.strategoRuntimeProvider = strategoRuntimeProvider;
    }

    @Override
    public String getId() { return "mb.tiger.spoofax.task.reusable.TigerListDefNames"; }

    @Override
    public @Nullable Result<String, ?> exec(ExecContext context, Supplier<? extends Result<IStrategoTerm, ?>> astSupplier) throws Exception {
        return context.require(astSupplier)
            .flatMapOrElse((ast) -> {
                final StrategoRuntime strategoRuntime = strategoRuntimeProvider.get();
                final String strategyId = "list-of-def-names";
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
