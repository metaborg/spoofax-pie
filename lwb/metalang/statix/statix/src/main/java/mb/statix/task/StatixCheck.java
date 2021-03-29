package mb.statix.task;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Messages;
import mb.common.message.Severity;
import mb.common.result.Result;
import mb.jsglr1.pie.JSGLR1ParseTaskInput;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import mb.statix.StatixClassLoaderResources;
import mb.statix.StatixScope;
import mb.statix.task.spoofax.StatixAnalyzeMultiWrapper;
import mb.statix.task.spoofax.StatixParseWrapper;
import mb.statix.util.StatixUtil;

import javax.inject.Inject;
import java.util.stream.Stream;

@StatixScope
public class StatixCheck implements TaskDef<StatixConfig, KeyedMessages> {
    private final StatixClassLoaderResources classLoaderResources;
    private final StatixParseWrapper parse;
    private final StatixAnalyzeMultiWrapper analyze;

    @Inject
    public StatixCheck(StatixClassLoaderResources classLoaderResources, StatixParseWrapper parse, StatixAnalyzeMultiWrapper analyze) {
        this.classLoaderResources = classLoaderResources;
        this.parse = parse;
        this.analyze = analyze;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public KeyedMessages exec(ExecContext context, StatixConfig config) throws Exception {
        context.require(classLoaderResources.tryGetAsLocalResource(getClass()), ResourceStampers.hashFile());
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();

        final ResourceWalker walker = StatixUtil.createResourceWalker();
        final ResourceMatcher matcher = StatixUtil.createResourceMatcher();
        final JSGLR1ParseTaskInput.Builder parseInputBuilder = parse.inputBuilder().rootDirectoryHint(config.rootDirectory);
        for(ResourcePath sourceOrIncludeDirectory : config.sourceAndIncludePaths()) {
            final HierarchicalResource directory = context.require(sourceOrIncludeDirectory, ResourceStampers.modifiedDirRec(walker, matcher));
            try(final Stream<? extends HierarchicalResource> stream = directory.walk(walker, matcher)) {
                stream.forEach(file -> {
                    final ResourcePath filePath = file.getPath();
                    final Messages messages = context.require(parseInputBuilder.withFile(filePath).buildMessagesSupplier());
                    messagesBuilder.addMessages(filePath, messages);
                });
            }
        }

        // TODO: this does not analyze all source and include directories
        final StatixAnalyzeMultiWrapper.Input analyzeInput = new StatixAnalyzeMultiWrapper.Input(config.rootDirectory, parse.createRecoverableMultiAstSupplierFunction(walker, matcher));
        final Result<StatixAnalyzeMultiWrapper.Output, ?> analysisResult = context.require(analyze, analyzeInput);
        analysisResult
            .ifOk(output -> {
                messagesBuilder.addMessages(output.result.messages);
                messagesBuilder.addMessages(output.messagesFromAstProviders);
            })
            .ifErr(e -> messagesBuilder.addMessage("Statix analysis failed", e, Severity.Error, config.rootDirectory));
        return messagesBuilder.build();
    }
}
