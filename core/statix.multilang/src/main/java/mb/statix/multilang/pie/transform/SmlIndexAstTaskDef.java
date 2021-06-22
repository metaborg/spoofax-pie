package mb.statix.multilang.pie.transform;

import mb.common.result.Result;
import mb.common.text.Text;
import mb.jsglr.common.ResourceKeyAttachment;
import mb.nabl2.terms.stratego.StrategoTermIndices;
import mb.pie.api.ExecContext;
import mb.pie.api.Function;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.spoofax.core.resource.ResourceTextSupplier;
import mb.stratego.common.StrategoRuntime;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import javax.inject.Provider;
import java.io.UncheckedIOException;

public abstract class SmlIndexAstTaskDef<E extends Exception> implements TaskDef<ResourceKey, Result<IStrategoTerm, ?>> {

    private final ITermFactory termFactory;
    private final Function<Supplier<Text>, Result<IStrategoTerm, E>> parse;

    public SmlIndexAstTaskDef(Provider<StrategoRuntime> strategoRuntimeProvider, Function<Supplier<Text>, Result<IStrategoTerm, E>> parse) {
        termFactory = strategoRuntimeProvider.get().getTermFactory();
        this.parse = parse;
    }

    @Override
    public Result<IStrategoTerm, ?> exec(ExecContext context, ResourceKey resourceKey) {
        try {
            return context.require(parse.createSupplier(new ResourceTextSupplier(resourceKey)))
                .map(ast -> {
                    ResourceKeyAttachment.setResourceKey(ast, resourceKey);
                    return StrategoTermIndices.index(ast, resourceKey.toString(), termFactory);
                });
        } catch(UncheckedIOException e) {
            return Result.ofErr(e.getCause());
        }
    }
}
