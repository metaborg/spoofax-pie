package mb.tiger.spoofax.task.reusable;

import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.common.StrategoUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import javax.inject.Provider;

public class TigerListLiteralVals implements TaskDef<Supplier<@Nullable IStrategoTerm>, @Nullable String> {
    private final Provider<StrategoRuntime> strategoRuntimeProvider;

    @Inject public TigerListLiteralVals(Provider<StrategoRuntime> strategoRuntimeProvider) {
        this.strategoRuntimeProvider = strategoRuntimeProvider;
    }

    @Override public String getId() {
        return "mb.tiger.spoofax.task.reusable.TigerListLiteralVals";
    }

    @Override
    public @Nullable String exec(ExecContext context, Supplier<@Nullable IStrategoTerm> astSupplier) throws Exception {
        final @Nullable IStrategoTerm ast = context.require(astSupplier);
        if(ast == null) {
            return null;
        }

        final StrategoRuntime strategoRuntime = strategoRuntimeProvider.get();
        final String strategyId = "list-of-literal-vals";
        final @Nullable IStrategoTerm result = strategoRuntime.invoke(strategyId, ast);
        if(result == null) {
            return null;
        }

        return StrategoUtil.toString(result, Integer.MAX_VALUE);
    }
}
