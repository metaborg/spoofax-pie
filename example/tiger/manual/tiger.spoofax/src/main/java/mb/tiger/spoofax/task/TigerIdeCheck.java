package mb.tiger.spoofax.task;

import mb.common.message.Messages;
import mb.common.message.MessagesBuilder;
import mb.common.message.Severity;
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
        final MessagesBuilder messagesBuilder = new MessagesBuilder();
        final ResourceStringSupplier stringSupplier = new ResourceStringSupplier(key);
        final Messages parseMessages = context.require(parse.createMessagesSupplier(stringSupplier));
        messagesBuilder.addMessages(parseMessages);
        final Result<TigerAnalyze.Output, ?> analysisResult = context.require(analyze, new TigerAnalyze.Input(key, parse.createRecoverableAstSupplier(stringSupplier)));
        analysisResult
            .ifOk(output -> messagesBuilder.addMessages(output.result.messages))
            .ifErr(e -> messagesBuilder.addMessage("Analysis failed", e, Severity.Error));
        return messagesBuilder.build();
    }
}
