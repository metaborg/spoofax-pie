package mb.tiger.spoofax.taskdef;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.constraint.common.ConstraintAnalyzer.SingleFileResult;
import mb.jsglr1.common.JSGLR1ParseResult;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;

public class TigerCheck implements TaskDef<ResourceKey, KeyedMessages> {
    private final TigerParse parse;
    private final TigerAnalyze analyze;

    @Inject public TigerCheck(TigerParse parse, TigerAnalyze analyze) {
        this.parse = parse;
        this.analyze = analyze;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public KeyedMessages exec(ExecContext context, ResourceKey key) throws Exception {
        final KeyedMessagesBuilder builder = new KeyedMessagesBuilder();
        final JSGLR1ParseResult parseOutput = context.require(parse, key);
        builder.addMessages(key, parseOutput.messages);
        final @Nullable SingleFileResult analysisOutput = context.require(analyze, key);
        if(analysisOutput != null) {
            builder.addMessages(key, analysisOutput.messages);
        }
        return builder.build();
    }
}
