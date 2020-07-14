package mb.statix.multilang.pie.transform;

import mb.common.result.Result;
import mb.jsglr.common.ResourceKeyAttachment;
import mb.nabl2.terms.stratego.StrategoTermIndices;
import mb.pie.api.ExecContext;
import mb.pie.api.Function;
import mb.pie.api.ResourceStringSupplier;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.stratego.common.StrategoRuntime;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import javax.inject.Provider;
import java.io.IOException;

public abstract class SmlIndexAstTaskDef<E extends Exception> implements TaskDef<ResourceKey, Result<IStrategoTerm, ?>> {

    private final ITermFactory termFactory;
    private final Function<Supplier<String>, Result<IStrategoTerm, E>> parse;

    public SmlIndexAstTaskDef(Provider<StrategoRuntime> strategoRuntimeProvider, Function<Supplier<String>, Result<IStrategoTerm, E>> parse) {
        termFactory = strategoRuntimeProvider.get().getTermFactory();
        this.parse = parse;
    }

    @Override
    public Result<IStrategoTerm, ?> exec(ExecContext context, ResourceKey resourceKey) {
        try {
            return context.require(parse.createSupplier(new ResourceStringSupplier(resourceKey)))
                .map(ast -> {
                    ResourceKeyAttachment.setResourceKey(ast, resourceKey);
                    return StrategoTermIndices.index(ast, resourceKey.toString(), termFactory);
                });
        } catch(IOException e) {
            return Result.ofErr(e);
        }
    }
}
