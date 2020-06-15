package mb.sdf3.spoofax.task;

import mb.jsglr.common.ResourceKeyAttachment;
import mb.nabl2.terms.stratego.StrategoTermIndices;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.stratego.common.StrategoRuntimeBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import javax.inject.Inject;

public class Sdf3IndexAst implements TaskDef<ResourceKey, @Nullable IStrategoTerm> {

    private Sdf3Parse parse;
    private ITermFactory tf;

    @Inject public Sdf3IndexAst(Sdf3Parse parse, StrategoRuntimeBuilder strategoRuntimeBuilder) {
        this.parse = parse;
        this.tf = strategoRuntimeBuilder.build().getTermFactory();
    }

    @Override public String getId() {
        return Sdf3IndexAst.class.getSimpleName();
    }

    @Override
    public @Nullable IStrategoTerm exec(ExecContext context, ResourceKey resourceKey) throws Exception {
        IStrategoTerm ast = context.require(parse.createNullableRecoverableAstSupplier(resourceKey));
        ResourceKeyAttachment.setResourceKey(ast, resourceKey);
        return StrategoTermIndices.index(ast, resourceKey.toString(), tf);
    }
}
