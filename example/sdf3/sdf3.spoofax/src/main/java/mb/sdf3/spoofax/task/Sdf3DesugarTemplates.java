package mb.sdf3.spoofax.task;

import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.spoofax.core.language.LanguageScope;
import mb.stratego.common.StrategoRuntime;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import javax.inject.Provider;

@LanguageScope
public class Sdf3DesugarTemplates implements TaskDef<Supplier<@Nullable IStrategoTerm>, @Nullable IStrategoTerm> {
    private final Provider<StrategoRuntime> strategoRuntimeProvider;

    @Inject public Sdf3DesugarTemplates(Provider<StrategoRuntime> strategoRuntimeProvider) {
        this.strategoRuntimeProvider = strategoRuntimeProvider;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public @Nullable IStrategoTerm exec(ExecContext context, Supplier<@Nullable IStrategoTerm> astSupplier) throws Exception {
        final @Nullable IStrategoTerm ast = context.require(astSupplier);
        if(ast == null) return null;
        final StrategoRuntime strategoRuntime = strategoRuntimeProvider.get();
        return strategoRuntime.invoke("desugar-templates", ast);
    }
}
