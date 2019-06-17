package mb.tiger.spoofax.taskdef;

import mb.common.style.Styling;
import mb.jsglr1.common.JSGLR1ParseResult;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.tiger.TigerStyler;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;

public class StylingTaskDef implements TaskDef<ResourceKey, @Nullable Styling> {
    private final Logger logger;
    private final ParseTaskDef parseTaskDef;
    private final TigerStyler styler;

    @Inject public StylingTaskDef(LoggerFactory loggerFactory, ParseTaskDef parseTaskDef, TigerStyler styler) {
        this.logger = loggerFactory.create(getClass());
        this.parseTaskDef = parseTaskDef;
        this.styler = styler;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public @Nullable Styling exec(ExecContext context, ResourceKey key) throws ExecException, InterruptedException {
        final JSGLR1ParseResult parseOutput = context.require(parseTaskDef, key);
        if(parseOutput.tokens == null) {
            return null;
        }
        logger.trace("Styling tokens:\n{}", parseOutput.tokens);
        return styler.style(parseOutput.tokens);
    }
}
