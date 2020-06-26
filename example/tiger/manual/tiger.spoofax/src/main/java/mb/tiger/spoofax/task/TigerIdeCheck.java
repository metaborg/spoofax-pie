package mb.tiger.spoofax.task;

import mb.common.message.Messages;
import mb.common.message.MessagesBuilder;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.ResourceStringSupplier;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.LanguageScope;
import mb.tiger.spoofax.task.reusable.TigerAnalyze;
import mb.tiger.spoofax.task.reusable.TigerParse;

import javax.inject.Inject;
import java.io.IOException;

@LanguageScope
public class TigerIdeCheck implements TaskDef<ResourceKey, Messages> {
    private final TigerParse parse;
    private final TigerAnalyze analyze;

    @Inject public TigerIdeCheck(TigerParse parse, TigerAnalyze analyze) {
        this.parse = parse;
        this.analyze = analyze;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Messages exec(ExecContext context, ResourceKey key) throws IOException {
        final MessagesBuilder builder = new MessagesBuilder();
        final ResourceStringSupplier stringProvider = new ResourceStringSupplier(key);
        final Messages parseMessages = context.require(parse.createMessagesSupplier(stringProvider));
        builder.addMessages(parseMessages);
        // TODO: propagate error/messages from analysis failure as well.
        final Result<TigerAnalyze.Output, ?> analysisOutput = context.require(analyze, new TigerAnalyze.Input(key, parse.createRecoverableAstSupplier(stringProvider)));
        analysisOutput.ifOk(output -> {
            builder.addMessages(output.result.messages);
        });
        return builder.build();
    }
}
