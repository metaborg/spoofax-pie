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

public class TigerListDefNames implements TaskDef<Supplier<? extends Result<IStrategoTerm, ?>>, Result<String, ? super Exception>> {
    private final Provider<StrategoRuntime> strategoRuntimeProvider;

    @Inject
    public TigerListDefNames(Provider<StrategoRuntime> strategoRuntimeProvider) {
        this.strategoRuntimeProvider = strategoRuntimeProvider;
    }

    @Override
    public String getId() { return "mb.tiger.spoofax.task.reusable.TigerListDefNames"; }

    @Override
    public @Nullable Result<String, ? super Exception> exec(ExecContext context, Supplier<? extends Result<IStrategoTerm, ?>> astSupplier) throws Exception {
        return context.require(astSupplier)
            .flatMapOrElse((ast) -> {
                final String strategyId = "list-of-def-names";
                try {
                    final @Nullable IStrategoTerm result = strategoRuntimeProvider.get().invoke(strategyId, ast);
                    return Result.ofNullableOrElse(result, () -> new Exception("Invoking '" + strategyId + "' on '" + ast + "' failed unexpectedly"));
                } catch(StrategoException e) {
                    return Result.ofErr(e);
                }
            }, Result::ofErr) // TODO: any way we don't have to use flatMapOrElse that threads the error to convert the type?
            .map(StrategoUtil::toString);
    }
}
