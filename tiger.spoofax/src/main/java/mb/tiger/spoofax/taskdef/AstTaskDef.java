package mb.tiger.spoofax.taskdef;

import mb.fs.api.path.FSPath;
import mb.jsglr1.common.JSGLR1ParseOutput;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.tiger.spoofax.taskdef.ParseTaskDef;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import javax.inject.Singleton;

public class AstTaskDef implements TaskDef<FSPath, @Nullable IStrategoTerm> {
    private final ParseTaskDef parseTaskDef;

    @Inject public AstTaskDef(ParseTaskDef parseTaskDef) {
        this.parseTaskDef = parseTaskDef;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public @Nullable IStrategoTerm exec(ExecContext context, FSPath path) throws Exception {
        final @Nullable JSGLR1ParseOutput parseOutput = context.require(parseTaskDef, path);
        if(parseOutput == null) {
            return null;
        }
        return parseOutput.ast;
    }
}
