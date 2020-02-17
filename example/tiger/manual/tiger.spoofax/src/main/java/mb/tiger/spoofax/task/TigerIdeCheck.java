package mb.tiger.spoofax.task;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.jsglr1.common.JSGLR1ParseResult;
import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.ResourceStringProvider;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.LanguageScope;
import mb.tiger.spoofax.task.reusable.TigerAnalyze;
import mb.tiger.spoofax.task.reusable.TigerParse;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;

@LanguageScope
public class TigerIdeCheck implements TaskDef<ResourceKey, KeyedMessages> {
    private final TigerParse parse;
    private final TigerAnalyze analyze;

    @Inject public TigerIdeCheck(TigerParse parse, TigerAnalyze analyze) {
        this.parse = parse;
        this.analyze = analyze;
    }

    @Override public String getId() {
        return "mb.tiger.spoofax.task.TigerCheck";
    }

    @Override
    public KeyedMessages exec(ExecContext context, ResourceKey key) throws ExecException, InterruptedException {
        final KeyedMessagesBuilder builder = new KeyedMessagesBuilder();
        final ResourceStringProvider stringProvider = new ResourceStringProvider(key);
        final JSGLR1ParseResult parseResult = context.require(parse, stringProvider);
        builder.addMessages(key, parseResult.getMessages());
        final TigerAnalyze.@Nullable Output output = context.require(analyze, new TigerAnalyze.Input(key, parse.createAstProvider(stringProvider)));
        if(output != null) {
            builder.addMessages(key, output.result.messages);
        }
        return builder.build();
    }
}
