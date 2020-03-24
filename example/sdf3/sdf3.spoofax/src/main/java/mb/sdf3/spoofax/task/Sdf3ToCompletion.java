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
public class Sdf3ToCompletion implements TaskDef<Supplier<@Nullable IStrategoTerm>, @Nullable IStrategoTerm> {
    private final Provider<StrategoRuntime> strategoRuntimeProvider;
    private final Sdf3DesugarTemplates desugarTemplates;

    @Inject public Sdf3ToCompletion(
        Provider<StrategoRuntime> strategoRuntimeProvider,
        Sdf3DesugarTemplates desugarTemplates
    ) {
        this.strategoRuntimeProvider = strategoRuntimeProvider;
        this.desugarTemplates = desugarTemplates;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public @Nullable IStrategoTerm exec(ExecContext context, Supplier<@Nullable IStrategoTerm> astSupplier) throws Exception {
        final @Nullable IStrategoTerm ast = context.require(desugarTemplates.createTask(astSupplier));
        if(ast == null) return null;
        final StrategoRuntime strategoRuntime = strategoRuntimeProvider.get();
        return strategoRuntime.invoke("module-to-new-cmp", ast);
    }
}
