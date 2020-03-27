package mb.sdf3.spoofax.task;

import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.stratego.common.StrategoRuntime;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Provider;

public abstract class StrategoTransformTaskDef implements TaskDef<Supplier<@Nullable IStrategoTerm>, @Nullable IStrategoTerm> {
    private final Provider<StrategoRuntime> strategoRuntimeProvider;
    private final String strategyName;

    public StrategoTransformTaskDef(Provider<StrategoRuntime> strategoRuntimeProvider, String strategyName) {
        this.strategoRuntimeProvider = strategoRuntimeProvider;
        this.strategyName = strategyName;
    }

    @Override
    public @Nullable IStrategoTerm exec(ExecContext context, Supplier<@Nullable IStrategoTerm> astSupplier) throws Exception {
        final @Nullable IStrategoTerm ast = context.require(astSupplier);
        if(ast == null) return null;
        final StrategoRuntime strategoRuntime = strategoRuntimeProvider.get();
        return strategoRuntime.invoke(strategyName, ast);
    }
}
