package mb.tiger.spoofax.taskdef;

import mb.common.token.Token;
import mb.fs.api.path.FSPath;
import mb.jsglr1.common.JSGLR1ParseOutput;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.tiger.spoofax.taskdef.ParseTaskDef;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.util.ArrayList;

public class TokenizerTaskDef implements TaskDef<FSPath, @Nullable ArrayList<Token>> {
    private final ParseTaskDef parseTaskDef;

    @Inject public TokenizerTaskDef(ParseTaskDef parseTaskDef) {
        this.parseTaskDef = parseTaskDef;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public @Nullable ArrayList<Token> exec(ExecContext context, FSPath path) throws Exception {
        final @Nullable JSGLR1ParseOutput parseOutput = context.require(parseTaskDef, path);
        if(parseOutput == null) {
            return null;
        }
        return parseOutput.tokens;
    }
}
