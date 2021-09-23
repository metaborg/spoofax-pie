package mb.tiger.spoofax.task;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Messages;
import mb.common.message.Severity;
import mb.common.result.Result;
import mb.jsglr.common.JsglrParseException;
import mb.jsglr.common.JsglrParseOutput;
import mb.jsglr.pie.JsglrParseTaskInput;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.tiger.spoofax.TigerScope;
import mb.tiger.spoofax.task.reusable.TigerAnalyzeMulti;
import mb.tiger.spoofax.task.reusable.TigerParse;

import javax.inject.Inject;
import java.io.IOException;

/**
 * @implNote Although Tiger is a single-file language, we implement the multi-file check variant here as well for
 * development/testing purposes.
 */
@TigerScope
public class TigerCheckMulti implements TaskDef<ResourcePath, KeyedMessages> {
    private final TigerParse parse;
    private final TigerGetSourceFiles getSourceFiles;
    private final TigerAnalyzeMulti analyze;

    @Inject
    public TigerCheckMulti(TigerParse parse, TigerGetSourceFiles getSourceFiles, TigerAnalyzeMulti analyze) {
        this.parse = parse;
        this.getSourceFiles = getSourceFiles;
        this.analyze = analyze;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public KeyedMessages exec(ExecContext context, ResourcePath input) throws IOException {
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();
        final JsglrParseTaskInput.Builder parseInputBuilder = parse.inputBuilder().rootDirectoryHint(input);
        for(ResourcePath file : context.require(getSourceFiles, input)) {
            final Result<JsglrParseOutput, JsglrParseException> result = context.require(parseInputBuilder.withFile(file).buildSupplier());
            messagesBuilder.addMessages(file, result.mapOrElse(v -> v.messages.asMessages(), e -> e.getOptionalMessages().map(KeyedMessages::asMessages).orElseGet(Messages::of)));
        }
        final TigerAnalyzeMulti.Input analyzeInput = new TigerAnalyzeMulti.Input(input, parse.createRecoverableMultiAstSupplierFunction(getSourceFiles.createFunction()));
        final Result<TigerAnalyzeMulti.Output, ?> analysisResult = context.require(analyze, analyzeInput);
        analysisResult
            .ifOk(output -> {
                messagesBuilder.addMessages(output.result.messages);
                messagesBuilder.addMessages(output.messagesFromAstProviders);
            })
            .ifErr(e -> messagesBuilder.addMessage("Project-wide analysis failed", e, Severity.Error, input));
        return messagesBuilder.build();
    }
}
