package mb.tiger.spoofax.task.reusable;

import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.common.StrategoRuntimeBuilder;
import mb.stratego.common.StrategoUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;

public class TigerListLiteralVals implements TaskDef<Supplier<@Nullable IStrategoTerm>, @Nullable String> {
    private final StrategoRuntimeBuilder strategoRuntimeBuilder;
    private final StrategoRuntime prototypeStrategoRuntime;

    @Inject public TigerListLiteralVals(
        StrategoRuntimeBuilder strategoRuntimeBuilder,
        StrategoRuntime prototypeStrategoRuntime
    ) {
        this.strategoRuntimeBuilder = strategoRuntimeBuilder;
        this.prototypeStrategoRuntime = prototypeStrategoRuntime;
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

        final StrategoRuntime strategoRuntime = strategoRuntimeBuilder.buildFromPrototype(prototypeStrategoRuntime);
        final String strategyId = "list-of-literal-vals";
        final @Nullable IStrategoTerm result = strategoRuntime.invoke(strategyId, ast);
        if(result == null) {
            return null;
        }

        return StrategoUtil.toString(result, Integer.MAX_VALUE);
    }
}
