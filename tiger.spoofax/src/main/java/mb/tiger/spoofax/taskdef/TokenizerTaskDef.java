package mb.tiger.spoofax.taskdef;

import mb.common.token.Token;
import mb.jsglr1.common.JSGLR1ParseResult;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.util.ArrayList;

public class TokenizerTaskDef implements TaskDef<ResourceKey, @Nullable ArrayList<Token>> {
    private final ParseTaskDef parseTaskDef;

    @Inject public TokenizerTaskDef(ParseTaskDef parseTaskDef) {
        this.parseTaskDef = parseTaskDef;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public @Nullable ArrayList<Token> exec(ExecContext context, ResourceKey key) throws Exception {
        final @Nullable JSGLR1ParseResult parseOutput = context.require(parseTaskDef, key);
        return parseOutput.tokens;
    }
}
