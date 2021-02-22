package mb.sdf3.task;

import mb.common.result.Result;
import mb.jsglr.common.ResourceKeyAttachment;
import mb.nabl2.terms.stratego.StrategoTermIndices;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.sdf3.Sdf3Scope;
import mb.stratego.common.StrategoRuntime;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import javax.inject.Inject;
import javax.inject.Named;

@Sdf3Scope
public class Sdf3IndexAst implements TaskDef<ResourceKey, Result<IStrategoTerm, ?>> {
    private final Sdf3Parse parse;
    private final ITermFactory termFactory;

    @Inject public Sdf3IndexAst(Sdf3Parse parse, @Named("prototype") StrategoRuntime prototype) {
        this.parse = parse;
        this.termFactory = prototype.getTermFactory();
    }

    @Override public String getId() {
        return Sdf3IndexAst.class.getSimpleName();
    }

    @Override
    public Result<IStrategoTerm, ?> exec(ExecContext context, ResourceKey resourceKey) throws Exception {
        return context.require(parse.createRecoverableAstSupplier(resourceKey))
            .map(ast -> {
                ResourceKeyAttachment.setResourceKey(ast, resourceKey);
                return StrategoTermIndices.index(ast, resourceKey.toString(), termFactory);
            });
    }
}
