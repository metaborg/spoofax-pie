package mb.tiger.spoofax.taskdef;

import mb.jsglr1.common.JSGLR1ParseResult;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.AstResult;

import javax.inject.Inject;

public class AstTaskDef implements TaskDef<ResourceKey, AstResult> {
    private final ParseTaskDef parseTaskDef;

    @Inject public AstTaskDef(ParseTaskDef parseTaskDef) {
        this.parseTaskDef = parseTaskDef;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public AstResult exec(ExecContext context, ResourceKey key) throws Exception {
        final JSGLR1ParseResult parseOutput = context.require(parseTaskDef, key);
        return new AstResult(parseOutput.ast, parseOutput.recovered);
    }
}
