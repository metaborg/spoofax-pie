package mb.esv.spoofax.task;

import mb.common.result.Result;
import mb.common.util.ListView;
import mb.esv.spoofax.EsvScope;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.TermFactory;
import org.spoofax.terms.util.TermUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;

@EsvScope
public class EsvCompile implements TaskDef<ListView<Supplier<? extends Result<IStrategoTerm, ?>>>, Result<IStrategoTerm, ?>> {
    @Inject public EsvCompile() {}

    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<IStrategoTerm, ?> exec(ExecContext context, ListView<Supplier<? extends Result<IStrategoTerm, ?>>> input) throws IOException {
        final ArrayList<IStrategoTerm> sections = new ArrayList<>();
        for(Supplier<? extends Result<IStrategoTerm, ?>> supplier : input) {
            final Result<IStrategoTerm, ?> result = context.require(supplier);
            if(result.isErr()) {
                return result;
            }
            final IStrategoTerm term = result.unwrapUnchecked();
            if(!TermUtils.isAppl(term, "Module", 3)) {
                return Result.ofErr(new Exception("Supplied term '" + term + "' is not a Module/3 term"));
            }
            sections.addAll(term.getSubterm(2).getSubterms());
        }
        final TermFactory termFactory = new TermFactory();
        return Result.ofOk(termFactory.makeAppl("Module", termFactory.makeString("editor"), termFactory.makeAppl("NoImports"), termFactory.makeList(sections)));
    }
}
