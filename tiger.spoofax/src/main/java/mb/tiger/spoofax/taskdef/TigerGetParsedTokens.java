package mb.tiger.spoofax.taskdef;

import mb.common.token.Token;
import mb.jsglr1.common.JSGLR1ParseResult;
import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.ResourceStringProvider;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.util.ArrayList;

public class TigerGetParsedTokens implements TaskDef<ResourceKey, @Nullable ArrayList<? extends Token<?>>> {
    private final TigerParse parse;

    @Inject public TigerGetParsedTokens(TigerParse parse) {
        this.parse = parse;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public @Nullable ArrayList<? extends Token<?>> exec(ExecContext context, ResourceKey key) throws ExecException, InterruptedException {
        final @Nullable JSGLR1ParseResult parseResult = context.require(parse, new ResourceStringProvider(key));
        return parseResult.getTokens().orElse(null);
    }
}
