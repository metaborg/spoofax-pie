package mb.tiger.spoofax.taskdef;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.constraint.common.ConstraintAnalyzer.SingleFileResult;
import mb.jsglr1.common.JSGLR1ParseResult;
import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.ResourceStringProvider;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;

public class TigerGetMessages implements TaskDef<ResourceKey, KeyedMessages> {
    private final TigerParse parse;
    private final TigerAnalyze analyze;

    @Inject public TigerGetMessages(TigerParse parse, TigerAnalyze analyze) {
        this.parse = parse;
        this.analyze = analyze;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public KeyedMessages exec(ExecContext context, ResourceKey key) throws ExecException, InterruptedException {
        final KeyedMessagesBuilder builder = new KeyedMessagesBuilder();
        final ResourceStringProvider stringProvider = new ResourceStringProvider(key);
        final JSGLR1ParseResult parseResult = context.require(parse, stringProvider);
        builder.addMessages(key, parseResult.getMessages());
        final @Nullable SingleFileResult analysisResult = context.require(analyze, new TigerAnalyze.Input(key, parse.createAstProvider(stringProvider)));
        //noinspection ConstantConditions
        if(analysisResult != null) {
            builder.addMessages(key, analysisResult.messages);
        }
        return builder.build();
    }
}
