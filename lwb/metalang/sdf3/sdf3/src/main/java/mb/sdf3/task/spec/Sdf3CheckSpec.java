package mb.sdf3.task.spec;

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
import mb.sdf3.Sdf3ClassLoaderResources;
import mb.sdf3.Sdf3Scope;
import mb.sdf3.task.Sdf3AnalyzeMulti;
import mb.sdf3.task.Sdf3Parse;
import mb.sdf3.task.util.Sdf3Util;

import javax.inject.Inject;
import java.util.stream.Stream;

@Sdf3Scope
public class Sdf3CheckSpec implements TaskDef<Sdf3SpecConfig, KeyedMessages> {
    private final Sdf3ClassLoaderResources classLoaderResources;
    private final Sdf3Parse parse;
    private final Sdf3AnalyzeMulti analyze;

    @Inject
    public Sdf3CheckSpec(Sdf3ClassLoaderResources classLoaderResources, Sdf3Parse parse, Sdf3AnalyzeMulti analyze) {
        this.classLoaderResources = classLoaderResources;
        this.parse = parse;
        this.analyze = analyze;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public KeyedMessages exec(ExecContext context, Sdf3SpecConfig input) throws Exception {
        context.require(classLoaderResources.tryGetAsLocalResource(getClass()), ResourceStampers.hashFile());
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();

        final ResourceWalker walker = Sdf3Util.createResourceWalker();
        final ResourceMatcher matcher = Sdf3Util.createResourceMatcher();
        final HierarchicalResource mainSourceDirectory = context.require(input.mainSourceDirectory, ResourceStampers.modifiedDirRec(walker, matcher));
        try(final Stream<? extends HierarchicalResource> stream = mainSourceDirectory.walk(walker, matcher)) {
            final JSGLR1ParseTaskInput.Builder parseInputBuilder = parse.inputBuilder().rootDirectoryHint(input.rootDirectory);
            stream.forEach(file -> {
                final ResourcePath filePath = file.getPath();
                final Messages messages = context.require(parseInputBuilder.withFile(filePath).buildMessagesSupplier());
                messagesBuilder.addMessages(filePath, messages);
            });
        }

        final Sdf3AnalyzeMulti.Input analyzeInput = new Sdf3AnalyzeMulti.Input(input.mainSourceDirectory, parse.createRecoverableMultiAstSupplierFunction(walker, matcher));
        final Result<Sdf3AnalyzeMulti.Output, ?> analysisResult = context.require(analyze, analyzeInput);
        analysisResult
            .ifOk(output -> {
                messagesBuilder.addMessages(output.result.messages);
                messagesBuilder.addMessages(output.messagesFromAstProviders);
            })
            .ifErr(e -> messagesBuilder.addMessage("SDF3 analysis failed", e, Severity.Error, input.mainSourceDirectory));
        return messagesBuilder.build();
    }
}
