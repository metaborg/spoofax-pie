package mb.esv.task;

import mb.common.result.Result;
import mb.esv.EsvScope;
import mb.esv.util.EsvUtil;
import mb.esv.util.EsvVisitor;
import mb.pie.api.ExecContext;
import mb.pie.api.STask;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.output.OutputStampers;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.TermFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;

@EsvScope
public class EsvCompile implements TaskDef<EsvConfig, Result<IStrategoTerm, ?>> {
    private final EsvParse parse;

    @Inject public EsvCompile(EsvParse parse) {
        this.parse = parse;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public Result<IStrategoTerm, ?> exec(ExecContext context, EsvConfig config) throws IOException {
        for(STask<?> origin : config.sourceFileOrigins) {
            context.require(origin, OutputStampers.inconsequential());
        }
        final ArrayList<IStrategoTerm> sections = new ArrayList<>();
        final EsvVisitor visitor = new EsvVisitor(parse, config.includeDirectorySuppliers, config.includeAstSuppliers) {
            @Override
            protected void acceptAst(IStrategoTerm ast) {
                sections.addAll(EsvUtil.getSectionsFromModuleTerm(ast));
            }
        };
        visitor.visitMainFile(context, config.mainFile);
        final TermFactory termFactory = new TermFactory();
        return Result.ofOk(termFactory.makeAppl("Module", termFactory.makeString("editor"), termFactory.makeAppl("NoImports"), termFactory.makeList(sections)));
    }
}
