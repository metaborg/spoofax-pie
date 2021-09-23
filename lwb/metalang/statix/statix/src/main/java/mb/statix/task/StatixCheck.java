package mb.statix.task;

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
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.ResourcePath;
import mb.statix.StatixClassLoaderResources;
import mb.statix.StatixScope;
import mb.statix.task.spoofax.StatixAnalyzeMultiWrapper;
import mb.statix.task.spoofax.StatixGetSourceFiles;
import mb.statix.task.spoofax.StatixParseWrapper;

import javax.inject.Inject;

@StatixScope
public class StatixCheck implements TaskDef<StatixConfig, KeyedMessages> {
    private final StatixClassLoaderResources classLoaderResources;
    private final StatixParseWrapper parse;
    private final StatixGetSourceFiles getSourceFiles;
    private final StatixAnalyzeMultiWrapper analyze;

    @Inject
    public StatixCheck(
        StatixClassLoaderResources classLoaderResources,
        StatixParseWrapper parse,
        StatixGetSourceFiles getSourceFiles,
        StatixAnalyzeMultiWrapper analyze
    ) {
        this.classLoaderResources = classLoaderResources;
        this.parse = parse;
        this.getSourceFiles = getSourceFiles;
        this.analyze = analyze;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public KeyedMessages exec(ExecContext context, StatixConfig input) throws Exception {
        context.require(classLoaderResources.tryGetAsLocalResource(getClass()), ResourceStampers.hashFile());
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();

        final JsglrParseTaskInput.Builder parseInputBuilder = parse.inputBuilder().rootDirectoryHint(input.rootDirectory);
        for(ResourcePath file : context.require(getSourceFiles, input.rootDirectory)) {
            final Result<JsglrParseOutput, JsglrParseException> result = context.require(parseInputBuilder.withFile(file).buildSupplier());
            messagesBuilder.addMessages(file, result.mapOrElse(v -> v.messages.asMessages(), e -> e.getOptionalMessages().map(KeyedMessages::asMessages).orElseGet(Messages::of)));
        }

        final StatixAnalyzeMultiWrapper.Input analyzeInput = new StatixAnalyzeMultiWrapper.Input(input.rootDirectory, parse.createRecoverableMultiAstSupplierFunction(getSourceFiles.createFunction()));
        final Result<StatixAnalyzeMultiWrapper.Output, ?> analysisResult = context.require(analyze, analyzeInput);
        analysisResult
            .ifOk(output -> {
                messagesBuilder.addMessages(output.result.messages);
                messagesBuilder.addMessages(output.messagesFromAstProviders);
            })
            .ifErr(e -> messagesBuilder.addMessage("Statix analysis failed", e, Severity.Error, input.rootDirectory));
        return messagesBuilder.build();
    }
}
