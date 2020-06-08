package mb.sdf3.spoofax.task;

import mb.jsglr.common.ResourceKeyAttachment;
import mb.nabl2.terms.stratego.StrategoTermIndices;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.pie.api.ValueSupplier;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.TermFactory;

import javax.inject.Inject;

public class Sdf3PreAnalysisTransform implements TaskDef<ResourceKey, @Nullable IStrategoTerm> {

    private Sdf3Parse parse;
    private Sdf3PreStatix preStatix;
    private TermFactory tf = new TermFactory();

    @Inject public Sdf3PreAnalysisTransform(Sdf3Parse parse, Sdf3PreStatix preStatix) {
        this.parse = parse;
        this.preStatix = preStatix;
    }

    @Override public String getId() {
        return Sdf3PreAnalysisTransform.class.getSimpleName();
    }

    @Override
    public @Nullable IStrategoTerm exec(ExecContext context, ResourceKey resourceKey) throws Exception {
        // TODO: try with mapInput?
        IStrategoTerm ast = context.require(parse.createNullableAstSupplier(resourceKey));
        ResourceKeyAttachment.setResourceKey(ast, resourceKey);
        IStrategoTerm indexedAst = StrategoTermIndices.index(ast, resourceKey.toString(), tf);
        return context.require(preStatix.createTask(new ValueSupplier<>(indexedAst)));
    }
}
