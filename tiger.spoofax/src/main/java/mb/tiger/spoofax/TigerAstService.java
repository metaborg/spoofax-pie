package mb.tiger.spoofax;

import mb.fs.api.path.FSPath;
import mb.jsglr1.common.JSGLR1ParseOutput;
import mb.pie.api.ExecException;
import mb.pie.api.exec.TopDownExecutor;
import mb.spoofax.core.language.AstService;
import mb.tiger.spoofax.pie.ParseTaskDef;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;

public class TigerAstService implements AstService {
    private final TopDownExecutor topDownExecutor;
    private final ParseTaskDef parseTaskDef;

    @Inject public TigerAstService(TopDownExecutor topDownExecutor, ParseTaskDef parseTaskDef) {
        this.topDownExecutor = topDownExecutor;
        this.parseTaskDef = parseTaskDef;
    }

    @Override public @Nullable IStrategoTerm getAst(FSPath path) {
        try {
            final @Nullable JSGLR1ParseOutput parseOutput =
                topDownExecutor.newSession().requireInitial(parseTaskDef.createTask(path));
            if(parseOutput == null) {
                return null;
            }
            return parseOutput.ast;
        } catch(ExecException e) {
            throw new RuntimeException("Getting AST for path '" + path + "' failed unexpectedly", e.getCause());
        }
    }
}
